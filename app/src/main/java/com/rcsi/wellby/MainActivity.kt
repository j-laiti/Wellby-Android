package com.rcsi.wellby
// main activity for the app which checks user permissions and authentications and displays the main screen or sign in screen
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.rcsi.wellby.hrvTab.helpers.PermissionHandler
import com.rcsi.wellby.signinSystem.AuthManager
import com.rcsi.wellby.signinSystem.SignInNavigation
import com.rcsi.wellby.signinSystem.views.TermsAndConditionsScreen
import com.rcsi.wellby.toolkitTab.toDoList.ToDoRepository
import com.rcsi.wellby.ui.theme.CustomAppTheme

class MainActivity : ComponentActivity() {
    private lateinit var permissionHandler: PermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionHandler = PermissionHandler(this, this)

        val app = application as WellbyApp
        val database = app.database
        val toDoRepository = ToDoRepository(database.toDoDao())


        setContent {
            val authManager: AuthManager by viewModels()
            val userSession = authManager.userSession.collectAsState().value
            val haveAcceptedTerms = remember { mutableStateOf(readTermsAccepted()) }

            CustomAppTheme {

                NotificationPermissionRequester()

                if (!haveAcceptedTerms.value) {
                    TermsAndConditionsScreen(onAccepted = {
                        haveAcceptedTerms.value = true
                        saveTermsAccepted(true)
                    })
                } else {
                    if (userSession != null) {
                        MainAppContent(authManager, toDoRepository, permissionHandler)
                    } else {
                        SignInNavigation(authManager, toDoRepository)
                    }
                }

            }
        }
    }

    private fun readTermsAccepted(): Boolean {
        val prefs = getSharedPreferences("prefs_name", Context.MODE_PRIVATE)
        return prefs.getBoolean("TermsAccepted", false)
    }

    private fun saveTermsAccepted(accepted: Boolean) {
        val prefs = getSharedPreferences("prefs_name", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putBoolean("TermsAccepted", accepted)
            apply()
        }
    }
}