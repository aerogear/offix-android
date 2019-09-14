import { conflictHandler } from "offix-conflicts-server";
import { GraphQLContext } from '../../context'

export const checkAndUpdateTask = {
  Mutation: {
    checkAndUpdateTask: async (_: any, clientData: any, context: GraphQLContext) => {
      console.log("Update", clientData)
      const task = await context.db('task').select()
        .where('id', clientData.id).then((rows) => rows[0])
      if (!task) {
        throw new Error(`Invalid ID for task object: ${clientData.id}`);
      }

      const conflictError = conflictHandler.checkForConflict(task, clientData);
      if (conflictError) {
        throw conflictError;
      }

      const update = await context.db('task').update(clientData)
        .where({
          'id': clientData.id
        }).returning('*').then((rows) => rows[0])

      return update;
    }
  }
}
