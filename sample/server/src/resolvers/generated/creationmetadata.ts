import { GraphQLContext } from '../../context'

 enum Subscriptions {
  NEW_CREATIONMETADATA = 'newcreationmetadata',
  UPDATED_CREATIONMETADATA = 'updatedcreationmetadata'
}

export const creationmetadataResolvers = {
  CreationMetadata: {
    createdBy: (parent: any, _: any, context: GraphQLContext) => {
      return context.db.select().from('user').where('creationmetadataId', '=', parent.id)
                                .then((result) => result[0])
    }
  },

  Query: {
    findCreationMetadatas: (_: any, args: any, context: GraphQLContext) => {
      return context.db.select().from('creationmetadata').where(args.fields)
    },
    findAllCreationMetadatas: (_: any, __: any, context: GraphQLContext) => {
      return context.db.select().from('creationmetadata')
    }
  },

  Mutation: {
    createCreationMetadata: async (_: any, args: any, context: GraphQLContext) => {
      const [ id ] = await context.db('creationmetadata').insert(args.input).returning('id')
      const result = await context.db.select().from('creationmetadata').where('id', '=', id)
      context.pubsub.publish(Subscriptions.NEW_CREATIONMETADATA, {
        newCreationMetadata: result[0]
      })
      return result[0]
    },
    updateCreationMetadata: (_: any, args: any, context: GraphQLContext) => {
      return context.db('creationmetadata').where('id', '=' , args.id).update(args.input).then( async () => {
        const result = await context.db.select().from('creationmetadata').where('id', '=' , args.id);
        context.pubsub.publish(Subscriptions.UPDATED_CREATIONMETADATA, {
          updatedCreationMetadata: result[0]
        })
        return result[0]
    })}
  },

  Subscription: {
    newCreationMetadata: {
      subscribe: (_: any, __: any, context: GraphQLContext) => {
        return context.pubsub.asyncIterator(Subscriptions.NEW_CREATIONMETADATA)
      }
    },
    updatedCreationMetadata: {
      subscribe: (_: any, __: any, context: GraphQLContext) => {
        return context.pubsub.asyncIterator(Subscriptions.UPDATED_CREATIONMETADATA)
      }
    }
  }
}
