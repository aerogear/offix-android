import gql from 'graphql-tag'

export const typeDefs = gql`
  type Task {
    id: ID!
    version: Int
    title: String!
    description: String!
    status: String!
    creationMetadata: CreationMetadata
    assignedTo: User
  }

  type CreationMetadata {
    createdDate: String
    taskId: ID!
    createdBy: User
  }

  type User {
    id: ID!
    firstName: String!
    lastName: String!
    title: String!
    email: String!
    taskId: ID!
    creationmetadataId: ID!
  }

  input TaskInput {
    version: Int
    title: String!
    description: String!
    status: String!
  }

  input CreationMetadataInput {
    createdDate: String
    taskId: ID!
  }

  input UserInput {
    firstName: String!
    lastName: String!
    title: String!
    email: String!
    taskId: ID!
    creationmetadataId: ID!
  }

  input TaskFilter {
    id: ID
    version: Int
    title: String
    description: String
    status: String
  }

  input CreationMetadataFilter {
    createdDate: String
    taskId: ID
  }

  input UserFilter {
    id: ID
    firstName: String
    lastName: String
    title: String
    email: String
    taskId: ID
    creationmetadataId: ID
  }

  type Query {
    findTasks(fields: TaskFilter!): [Task!]!
    findCreationMetadatas(fields: CreationMetadataFilter!): [CreationMetadata!]!
    findUsers(fields: UserFilter!): [User!]!
    findAllTasks: [Task!]!
    findAllCreationMetadatas: [CreationMetadata!]!
    findAllUsers: [User!]!
  }

  type Mutation {
    createTask(input: TaskInput!): Task!
    createCreationMetadata(input: CreationMetadataInput!): CreationMetadata!
    createUser(input: UserInput!): User!
    updateTask(id: ID!, input: TaskInput!): Task!
    updateCreationMetadata(id: ID!, input: CreationMetadataInput!): CreationMetadata!
    updateUser(id: ID!, input: UserInput!): User!
    ## Custom mutations
    checkAndUpdateTask(id: ID!, title: String, description: String, version: Int!, status: String!): Task
  }

  type Subscription {
    newTask: Task!
    newCreationMetadata: CreationMetadata!
    newUser: User!
    updatedTask: Task!
    updatedCreationMetadata: CreationMetadata!
    updatedUser: User!
  }
`
