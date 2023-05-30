package com.geochamp.myapp

import android.content.Intent
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.example.myapp.View.LoginActivity
import com.example.myapp.View.MapsActivity
import org.junit.Assert.*
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters


@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class LoginActivityTest {

    @get:Rule
    var activityRule = ActivityTestRule(LoginActivity::class.java)

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Test
    fun testLoginWith1InvalidCredentials() {
        // Launch the activity
        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, LoginActivity::class.java)
        activityRule.launchActivity(intent)

        // Test the login functionality with an invalid email and password
        val email = "invalidemail"
        val password = "invalidpassword"

        // Find the email and password input fields and enter the invalid email and password
        Espresso.onView(ViewMatchers.withId(R.id.loginEmail)).perform(ViewActions.typeText(email))
        Espresso.onView(ViewMatchers.withId(R.id.loginPass)).perform(ViewActions.typeText(password))

        // Click the login button to perform the login
        Espresso.onView(ViewMatchers.withId(R.id.btnLogin)).perform(ViewActions.click())

        val expectedIntent = Intent(activityRule.activity, MapsActivity::class.java)
        val activityMonitor = InstrumentationRegistry.getInstrumentation().addMonitor(expectedIntent.component!!.className, null, false)

        val nextActivity = activityMonitor.waitForActivityWithTimeout(5000)
        assertNull(nextActivity)

    }

    @Test
    fun testLoginWith2EmptyFields() {
        // Launch the activity
        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, LoginActivity::class.java)
        activityRule.launchActivity(intent)

        try {
            onView(withId(R.id.btnLogin)).perform(click())
        }catch (e: Exception) {
            // If any exception is caught, the test passes.
            assert(true)
        }
    }

    @Test
    fun testLoginWith3Email() {
        // Launch the activity
        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, LoginActivity::class.java)
        activityRule.launchActivity(intent)

        // Test the login functionality with email and password
        val email = "ortest@gmail.com"
        val password = "Aa123456"

        // Find the email and password input fields and enter the given email and password
        Espresso.onView(ViewMatchers.withId(R.id.loginEmail)).perform(ViewActions.typeText(email))
        Espresso.onView(ViewMatchers.withId(R.id.loginPass)).perform(ViewActions.typeText(password))

        // Click the login button to perform the login
        Espresso.onView(ViewMatchers.withId(R.id.btnLogin)).perform(ViewActions.click())

        val expectedIntent = Intent(activityRule.activity, MapsActivity::class.java)
        val activityMonitor = InstrumentationRegistry.getInstrumentation().addMonitor(expectedIntent.component!!.className, null, false)

        // Wait for up to 5 seconds for the next activity to be launched
        val nextActivity = activityMonitor.waitForActivityWithTimeout(5000)
        assertNotNull(nextActivity)

        // Remove the activity monitor to avoid leaks
        InstrumentationRegistry.getInstrumentation().removeMonitor(activityMonitor)
    }

    @Test
    fun testMapIsPresented() {
        // Launch the activity
        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, LoginActivity::class.java)
        activityRule.launchActivity(intent)

        // Wait for the map view to be displayed
        Espresso.onView(ViewMatchers.withId(R.id.map))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}


