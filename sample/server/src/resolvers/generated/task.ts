import { GraphQLContext } from '../../context'

 enum Subscriptions {
  NEW_TASK = 'newtask',
  UPDATED_TASK = 'updatedtask'
}

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
      const [ id ] = await context.db('task').insert(args.input).returning('id')
      const result = await context.db.select().from('task').where('id', '=', id)
      context.pubsub.publish(Subscriptions.NEW_TASK, {
        newTask: result[0]
      })
      return result[0]
    },
    updateTask: (_: any, args: any, context: GraphQLContext) => {
      return context.db('task').where('id', '=' , args.id).update(args.input).then( async () => {
        const result = await context.db.select().from('task').where('id', '=' , args.id);
        context.pubsub.publish(Subscriptions.UPDATED_TASK, {
          updatedTask: result[0]
        })
        return result[0]
    })}
  },

  Subscription: {
    newTask: {
      subscribe: (_: any, __: any, context: GraphQLContext) => {
        return context.pubsub.asyncIterator(Subscriptions.NEW_TASK)
      }
    },
    updatedTask: {
      subscribe: (_: any, __: any, context: GraphQLContext) => {
        return context.pubsub.asyncIterator(Subscriptions.UPDATED_TASK)
      }
    }
  }
}
