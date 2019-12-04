package org.aerogear.graphqlandroid.managers

import android.content.Context
import android.util.Log
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.rx2.Rx2Apollo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import org.aerogear.graphqlandroid.*

object SubscriptionManager{
    private val disposables = CompositeDisposable()
    private val TAG = "SubscriptionManager"

    public fun subscriptionNewTask(context: Context) {
        val subscription = NewTaskSubscription()
        val subscriptionCall = Utils.getApolloClient(context)
            ?.subscribe(subscription)

        disposables.add(
            Rx2Apollo.from<NewTaskSubscription.Data>(subscriptionCall!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                    object : DisposableSubscriber<Response<NewTaskSubscription.Data>>() {
                        override fun onNext(response: Response<NewTaskSubscription.Data>) {
                            val res = response.data()?.newTask()
                            res?.let {
                            }
                        }

                        override fun onError(e: Throwable) {
                            Log.e(TAG, e.message, e)
                        }

                        override fun onComplete() {
                            Log.e(TAG, "Subscription new task added exhausted")
                        }
                    }
                )
        )
    }

    public fun subscriptionUpdateTask(context: Context) {

        val subscription = UpdatedTaskSubscription()
        val subscriptionCall = Utils.getApolloClient(context)
            ?.subscribe(subscription)

        disposables.add(
            Rx2Apollo.from<UpdatedTaskSubscription.Data>(subscriptionCall!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                    object : DisposableSubscriber<Response<UpdatedTaskSubscription.Data>>() {
                        override fun onNext(response: Response<UpdatedTaskSubscription.Data>) {
                            val res = response.data()?.updatedTask()
                            res?.let {
                                Log.e(
                                    TAG,
                                    " inside subscriptionUpdateTask ${it.title()} mutated upon updating"
                                )
                            }
                        }

                        override fun onError(e: Throwable) {
                            Log.e(TAG, e.message, e)
                        }

                        override fun onComplete() {
                            Log.e(TAG, "subscriptionUpdateTask exhausted")
                        }
                    }
                )
        )
    }

    public fun subscriptionNewUser(context: Context) {
        val subscription = NewUserSubscription()
        val subscriptionCall = Utils.getApolloClient(context)
            ?.subscribe(subscription)

        disposables.add(
            Rx2Apollo.from<NewUserSubscription.Data>(subscriptionCall!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                    object : DisposableSubscriber<Response<NewUserSubscription.Data>>() {
                        override fun onNext(response: Response<NewUserSubscription.Data>) {
                            val res = response.data()?.newUser()
                            res?.let {
                                Log.e(
                                    TAG,
                                    " inside subscriptionNewUser ${it.title()} mutated upon new title"
                                )
                            }
                        }

                        override fun onError(e: Throwable) {
                            Log.e(TAG, e.message, e)
                        }

                        override fun onComplete() {
                            Log.e(TAG, "Subscription new user added exhausted")
                        }
                    }
                )
        )
    }

    public fun subscriptionUpdateUser(context: Context) {

        val subscription = UpdatedUserSubscription()
        val subscriptionCall = Utils.getApolloClient(context)
            ?.subscribe(subscription)

        disposables.add(
            Rx2Apollo.from<UpdatedUserSubscription.Data>(subscriptionCall!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                    object : DisposableSubscriber<Response<UpdatedUserSubscription.Data>>() {
                        override fun onNext(response: Response<UpdatedUserSubscription.Data>) {

                            val res = response.data()?.updatedUser()
                            res?.let {
                                Log.e(
                                    TAG,
                                    " inside subscriptionUpdateUser ${it.title()} mutated upon updating"
                                )
                            }
                        }

                        override fun onError(e: Throwable) {
                            Log.e(TAG, e.message, e)
                        }

                        override fun onComplete() {
                            Log.e(TAG, "subscriptionUpdateUser exhausted")
                        }
                    }
                )
        )
    }
}