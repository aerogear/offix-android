package org.aerogear.graphqlandroid

import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation

interface CustomMutation : Mutation<Operation.Data, Void, Operation.Variables>