import { GraphQLContext } from '../../context'

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
      const [ id ] = await context.db('user').insert(args.input).returning('id')
      const result = await context.db.select().from('user').where('id', '=', id)
      return result[0]
    },
    updateUser: (_: any, args: any, context: GraphQLContext) => {
      return context.db('user').where('id', '=' , args.id).update(args.input).then( async () => {
        const result = await context.db.select().from('user').where('id', '=' , args.id);
        return result[0]
    })}
  }
}
