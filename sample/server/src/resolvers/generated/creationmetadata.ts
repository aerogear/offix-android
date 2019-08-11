import { GraphQLContext } from '../../context'

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
      const result = await context.db('creationmetadata').insert(args.input).returning('*')
      return result[0]
    },
    updateCreationMetadata: (_: any, args: any, context: GraphQLContext) => {
      return context.db('creationmetadata').where('id', '=' , args.id).update(args.input).then( async () => {
        const result = await context.db.select().from('creationmetadata').where('id', '=' , args.id);
        return result[0]
    })}
  }
}
