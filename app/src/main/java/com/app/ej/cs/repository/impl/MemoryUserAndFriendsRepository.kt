package com.app.ej.cs.repository.impl

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import com.app.ej.cs.conf.TAG
import com.app.ej.cs.init.InitApp.Companion.appContext
import com.app.ej.cs.repository.UsersAndFriendsRepository
import com.app.ej.cs.repository.entity.*
import com.app.ej.cs.ui.account.LoginActivityMain
import com.app.ej.cs.utils.NetworkUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson


class MemoryUserAndFriendsRepository(appContext: Context) : UsersAndFriendsRepository {

  private val PREFNAME: String = "local_user"
  private val context    = appContext
  private var usersMap   = mutableMapOf<Int, User>()
  private var friendsMap = mutableMapOf<Int, Friend>()

  private val mFirestore: FirebaseFirestore = Firebase.firestore
  private lateinit var auth: FirebaseAuth

  private lateinit var userId: String

  private var usersList: MutableList<User>? = null

  private var friendsList: MutableList<Friend>? = null


    private fun readFromFirestore() {

        auth     = Firebase.auth
        val user = auth.currentUser

        if (user == null) {
            Log.e("ATTENTION ATTENTION", "user == null")
            return
        }
        else {
            userId = user.uid;
        }

        mFirestore
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->

                try {

                    if (document != null) {

                        val userHashMap = document.data

                        Log.e("userHashMap", userHashMap.toString())

                        val allInfo = document.toObject(UserAndFriendInfo::class.java) ?: UserAndFriendInfo()

                        Log.e("ATTENTION ATTENTION", "allInfo.toString(): ${allInfo.toString()}")

                        usersMap.clear()
                        friendsMap.clear()

                        usersList = allInfo.usersList
                        assignValues("User")

                        if (allInfo.friendList != null) {
                            friendsList = allInfo.friendList!!
                            assignValues("Friend")
                        }

                    }

                }
                catch (ex: Exception){
                    Log.e(TAG, ex.toString())
                }


            }
            .addOnFailureListener { e -> Log.e(TAG, "Error writing document", e) }


    }

  private fun assignValues(type: String) {


    if (type == "User") {

      var i: Int = 0

      usersList!!.forEach { user ->

        userInsert(

            User(
                uid= user.uid,
                index = i,
                description = "Phone $i",
                folded = user.folded,
                created = user.created,
                name = user.name,
                email = user.email,
                phone = user.phone,
                network = user.network,
                pin = user.pin,
                bank1 = user.bank1,
                bank2 = user.bank2,
                bank3 = user.bank3,
                bank4 = user.bank4,
                smartCardNumber1 = user.smartCardNumber1,
                smartCardNumber2 = user.smartCardNumber2,
                smartCardNumber3 = user.smartCardNumber3,
                smartCardNumber4 = user.smartCardNumber4,
                meterNumber1 = user.meterNumber1,
                meterNumber2 = user.meterNumber2,
                meterNumber3 = user.meterNumber3,
                )
        )

        i++

      }

    }

    else if (type == "Friend") {

      var j: Int = 0

      friendsList!!.forEach { friend ->

        friendInsert(

                Friend(
                    index = j,
                    description = "Friend $j",
                    folded = friend.folded,
                    name = friend.name,
                    phone1 = friend.phone1,
                    phone2 = friend.phone1,
                    phone3 = friend.phone3,
                    network1 = friend.network1,
                    network2 = friend.network2,
                    network3 = friend.network3,
                    bank1 = friend.bank1,
                    bank2 = friend.bank2,
                    bank3 = friend.bank3,
                    bank4 = friend.bank4,
                    accountNumber1 = friend.accountNumber1,
                    accountNumber2 = friend.accountNumber2,
                    accountNumber3 = friend.accountNumber3,
                    accountNumber4 = friend.accountNumber4,
                    )
        )

        j++

      }



    }



  }

private fun returnToLogin() {

    val intent: Intent = Intent(context, LoginActivityMain::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    appContext.startActivity(intent)

}

  private fun readFromLocal() {

      val sharedPref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

      if (!sharedPref.getBoolean(PREFNAME, false)) {
          return
      }
      val gson = Gson()

      var allInfoJson: String? = null

      allInfoJson = sharedPref.getString("allInfoSaved", "defaultAll")

      if (allInfoJson != "defaultAll") {

          val allInfo = gson.fromJson(allInfoJson, UserAndFriendInfo::class.java)

          Log.e("ATTENTION ATTENTION", "local allInfo.toString(): ${allInfo.toString()}")

          usersList   = allInfo.usersList
          assignValues("User")

          if (allInfo.friendList != null) {
              friendsList = allInfo.friendList!!
              assignValues("Friend")
          }

      }

  }


  fun isNetworkAvailable(handler: Handler, timeout: Int)  {

    object : Thread() {
      private var responded: Boolean = false
      override fun run() {

        object : Thread() {
          override fun run() {}
        }.start()
        try {
          var waited: Int = 0
          while (!responded && waited < timeout) {
            sleep(100)
            if (!responded) {
              waited += 100
            }
          }
        } catch (e: InterruptedException) {
        }
        finally {
          if (!responded) {
            handler.sendEmptyMessage(0)
          } else {
            handler.sendEmptyMessage(1)
          }
        }
      }
    }.start()
  }


  private fun runStartUp() {

    readFromLocal()

      val networkUtil: NetworkUtil = NetworkUtil()

    if (networkUtil.isOnline(context)) {
      readFromFirestore()
    }

  }


  init {

    runStartUp()

  }



  override fun friendById(id: Int): Friend? = friendsMap[id]

    // TODO DONE
    override fun friendList(): MutableList<Friend> {

        val sharedPref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

        val gson = Gson()

        var allInfoJson: String? = null

        allInfoJson = sharedPref.getString("allInfoSaved", "defaultAll")

        if (allInfoJson != "defaultAll") {

            val allInfo = gson.fromJson(allInfoJson, UserAndFriendInfo::class.java)

            Log.e("ATTENTION ATTENTION", "local allInfo.toString(): ${allInfo.toString()}")

            usersList   = allInfo.usersList
            assignValues("User")

            if (allInfo.friendList != null) {
                friendsList = allInfo.friendList!!
                assignValues("Friend")
            }

        }

        return if (friendsList != null) {

            friendsList!!

        } else {

            mutableListOf()

        }

    }

    // TODO DONE
    override fun friendListUnsaved(): MutableList<Friend> {

        val sharedPref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

        val gson = Gson()

        var allInfoJson: String? = null

        allInfoJson = sharedPref.getString("allInfoUnsaved", "defaultAll")

        if (allInfoJson != "defaultAll") {

            val allInfo = gson.fromJson(allInfoJson, UserAndFriendInfo::class.java)

            Log.e("ATTENTION ATTENTION", "local unsaved allInfo.toString(): ${allInfo.toString()}")

            usersList   = allInfo.usersList
            assignValues("User")

            if (allInfo.friendList != null) {
                friendsList = allInfo.friendList!!
                assignValues("Friend")
            }

        }

        return if (friendsList != null) {

            friendsList!!

        } else {

            mutableListOf()

        }

    }

  override fun friendInsert(friend: Friend) {
      friendsMap[friend.index] = friend
  }

  override fun userById(id: Int): User? = usersMap[id]

  override fun userList(): MutableList<User> {

      val sharedPref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

      val gson = Gson()

      var allInfoJson: String? = null

      allInfoJson = sharedPref.getString("allInfoSaved", "defaultAll")

      if (allInfoJson != "defaultAll") {

          val allInfo = gson.fromJson(allInfoJson, UserAndFriendInfo::class.java)

          Log.e("ATTENTION ATTENTION", "local allInfo.toString(): ${allInfo.toString()}")

          usersList   = allInfo.usersList
          assignValues("User")

          if (allInfo.friendList != null) {
              friendsList = allInfo.friendList!!
              assignValues("Friend")
          }

      }

        return if (usersList != null) {

            usersList!!

        } else {

            mutableListOf()

        }

  }

  override fun userInsert(user: User) {
      usersMap[user.index] = user
  }

}