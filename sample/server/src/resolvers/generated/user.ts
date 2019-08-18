import { GraphQLContext } from '../../context'

enum Subscriptions {
  NEW_USER = 'newuser',
  UPDATED_USER = 'updateduser'
}

export const userResolvers = {
  Query: {
    findUsers: (_: any, args: any, context: GraphQLContext) => {
      return context.db.select().from('user').where(args.fields)
    },
    findAllUsers: (_: any, __: any, context: GraphQLContext) => {
      return context.db.select().from('user')
    }
  },

  Mutation: {
    createUser: async (_: any, args: any, context: GraphQLContext) => {
      const result = await context.db('user').insert(args.input).returning('*')
      context.pubsub.publish(Subscriptions.NEW_USER, {
        newUser: result[0]
      })
      return result[0]
    },
    updateUser: (_: any, args: any, context: GraphQLContext) => {
      return context.db('user').where('id', '=' , args.id).update(args.input).then( async () => {
        const result = await context.db.select().from('user').where('id', '=' , args.id);
        context.pubsub.publish(Subscriptions.UPDATED_USER, {
          updatedUser: result[0]
        })
        return result[0]
    })}
  },

  Subscription: {
    newUser: {
      subscribe: (_: any, __: any, context: GraphQLContext) => {
        return context.pubsub.asyncIterator(Subscriptions.NEW_USER)
      }
    },
    updatedUser: {
      subscribe: (_: any, __: any, context: GraphQLContext) => {
        return context.pubsub.asyncIterator(Subscriptions.UPDATED_USER)
      }
    }
  }
}
