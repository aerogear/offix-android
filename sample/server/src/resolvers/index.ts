import { creationmetadataResolvers } from './generated/creationmetadata'
import { taskResolvers } from './generated/task'
import { userResolvers } from './generated/user'

import { customResolvers } from './custom'

export const resolvers = [creationmetadataResolvers, taskResolvers, userResolvers, ...customResolvers]
