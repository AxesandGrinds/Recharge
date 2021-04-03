package com.app.ej.cs.repository.impl

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.util.Log
import com.app.ej.cs.conf.TAG
import com.app.ej.cs.init.InitApp.Companion.appContext
import com.app.ej.cs.repository.UsersAndFriendsRepository
import com.app.ej.cs.repository.entity.*
import com.app.ej.cs.ui.account.LoginActivity
import com.app.ej.cs.utils.NetworkUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

//import com.google.firebase.auth.FirebaseAuth

/**
 * Repository implementation in memory
 */
class MemoryUserAndFriendsRepository(appContext: Context) : UsersAndFriendsRepository {

  private val PREFNAME: String = "local_user"
  private val context    = appContext
  private var usersMap   = mutableMapOf<Int, User>()
  private var friendsMap = mutableMapOf<Int, Friend>()

  // Access a Cloud Firestore instance from your Activity
  private val mFirestore: FirebaseFirestore = Firebase.firestore
  private lateinit var auth: FirebaseAuth

  private lateinit var userId: String

  private lateinit var usersList: MutableList<User>

  private lateinit var friendsList: MutableList<Friend>


    private fun readFromFirestore() {

        auth     = Firebase.auth
        val user = auth.currentUser

        if (user == null) {
//            returnToLogin()
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

//                  val data = Gson().toJson(userHashMap)
//                  val docsData = Gson().fromJson<UserAndFriendInfo>(data, UserAndFriendInfo::class.java)
//
//                  usersList = docsData.usersList
//                  assignValues("User")
//
//                  if (docsData.friendsList != null) {
//                    friendsList = docsData.friendsList!!
//                    assignValues("Friend")
//                  }

                    }

                }
                catch (ex: Exception){
                    Log.e(TAG, ex.toString())
                }


            }
            .addOnFailureListener { e -> Log.e(TAG, "Error writing document", e)
            }


    }

  private fun assignValues(type: String) {


    if (type == "User") {

      var i: Int = 0

      usersList.forEach { user ->

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

      friendsList.forEach { friend ->

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

    val intent: Intent = Intent(context, LoginActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    appContext.startActivity(intent)

}

  private fun readFromLocal() {

      val sharedPref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

      if (!sharedPref.getBoolean(PREFNAME, false)) {
//          returnToLogin()
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

//    var usersJson:   String? = null
//    var friendsJson: String? = null
//    usersJson   = sharedPref.getString("users",   "defaultUser")
//    friendsJson = sharedPref.getString("friends", "defaultFriend")
//    val usersInfo   = gson.fromJson(usersJson,   UsersInfo::class.java)
//    val friendsInfo = gson.fromJson(friendsJson, FriendInfo::class.java)
//    usersList   = usersInfo.userList
//    friendsList = friendsInfo.friendsList

  }


  fun isNetworkAvailable(handler: Handler, timeout: Int)  {
    // ask fo message '0' (not connected) or '1' (connected) on 'handler'
    // the answer must be send before before within the 'timeout' (in milliseconds)
    object : Thread() {
      private var responded: Boolean = false
      override fun run() {
        // set 'responded' to TRUE if is able to connect with google mobile (responds fast)
        object : Thread() {
          override fun run() {

              /// New but not sure
//            val url = URL("http://www.google.com/")
//
//            with(url.openConnection() as HttpURLConnection) {
//
//              requestMethod = "GET"  // optional default is GET
//
//              println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")
//
//              inputStream.bufferedReader().use {
//
//                var googleString: String = it.readText()
//
//                }
//
//              }


              /// Original but outdated
//            val requestForTest: HttpGet = HttpGet("http://m.google.com")
//
//            try {
//              DefaultHttpClient().execute(requestForTest) // can last...
//              responded = true
//            }
//            catch (e: java.lang.Exception) {
//            }



          }
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
        } // do nothing
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

      return friendsList

//      return friendsMap.values.sortedBy { it.index }.toMutableList()

  }

  override fun friendInsert(friend: Friend) {
      friendsMap[friend.index] = friend
  }

  override fun userById(id: Int): User? = usersMap[id]

    // TODO DONE
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

      return usersList

//      return usersMap.values.sortedBy { it.index }.toMutableList()

  }

  override fun userInsert(user: User) {
      usersMap[user.index] = user
  }

}