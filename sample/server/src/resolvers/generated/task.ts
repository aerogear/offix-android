import { GraphQLContext } from '../../context'

export const taskResolvers = {
  Task: {
    creationMetadata: (parent: any, _: any, context: GraphQLContext) => {
      return context.db.select().from('creationmetadata').where('taskId', '=', parent.id)
                                .then((result) => result[0])
    },
    assignedTo: (parent: any, _: any, context: GraphQLContext) => {
      return context.db.select().from('user').where('taskId', '=', parent.id)
                                .then((result) => result[0])
    }
  },

  Query: {
    findTasks: (_: any, args: any, context: GraphQLContext) => {
      return context.db.select().from('task').where(args.fields)
    },
    findAllTasks: (_: any, __: any, context: GraphQLContext) => {
      return context.db.select().from('task')
    }
  },

  Mutation: {
    createTask: async (_: any, args: any, context: GraphQLContext) => {
      const result = await context.db('task').insert(args.input).returning('*')
      return result[0]
    },
    updateTask: (_: any, args: any, context: GraphQLContext) => {
      return context.db('task').where('id', '=' , args.id).update(args.input).then( async () => {
        const result = await context.db.select().from('task').where('id', '=' , args.id);
        return result[0]
    })}
  }
}
