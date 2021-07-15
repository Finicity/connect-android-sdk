package com.finicity.connect.sdk;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.web.webdriver.DriverAtoms;
import android.support.test.espresso.web.webdriver.Locator;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isJavascriptEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webClick;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webScrollIntoView;
import static org.junit.Assert.fail;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class ConnectActivityTest {

    // Generate a 2.0 Connect url using Postman and set goodUrl to it before running UI unit tests.
    private static final String goodUrl = "https://connect2.finicity.com?consumerId=a4aae5b4dc6a29375e6df3e873452bcb&customerId=5009798522&partnerId=2445582695152&redirectUri=http%3A%2F%2Flocalhost%3A3001%2Fcustomers%2FredirectHandler&signature=92aa974cad1a32399877bbd3d81e06fc011ca360b08004bdc0804766add48659&timestamp=1626375845429&ttl=1626383045429";
    private static final String badExpiredUrl = "https://connect2.finicity.com?consumerId=dbceec20d8b97174e6aed204856f5a55&customerId=1016927519&partnerId=2445582695152&redirectUri=http%3A%2F%2Flocalhost%3A3001%2Fcustomers%2FredirectHandler&signature=abb1762e5c640f02823c56332daede3fe2f2143f4f5b8be6ec178ac72d7dbc5a&timestamp=1607806595887&ttl=1607813795887";
    private WebEventIdlingResource mIdlingResource;

    @Before
    public void setup() {
        mIdlingResource = new WebEventIdlingResource();
        IdlingRegistry.getInstance().register(mIdlingResource);
        Connect.runningUnitTest = true;
    }

    @After
    public void teardown() {
        Connect.runningUnitTest = false;
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource);
        }
    }

    @Test
    public void test01ConnectWithExpiredUrl() {
        Connect.start(InstrumentationRegistry.getContext(), badExpiredUrl, new TestEventHandler());

        mIdlingResource.waitForEvent("error");
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Exit")).perform(webClick());
    }

    @Test
    public void test02ConnectWithGoodUrlThenCancel() throws InterruptedException {

        Connect.start(InstrumentationRegistry.getContext(), goodUrl, new TestEventHandler());

        mIdlingResource.waitForEvent("search");
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Exit")).perform(webClick());

        Thread.sleep(1000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Yes")).perform(webClick());
    }

    @Test
    public void test03ConnectWithGoodUrlThenBackButton() {

        Connect.start(InstrumentationRegistry.getContext(), goodUrl, new TestEventHandler());

        // Wait for Route search or let it timeout
        mIdlingResource.waitForEvent("search");
        onWebView()
                .withElement(findElement(Locator.NAME, "Search for your bank"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("FinBank"))
                .perform(webClick());

        // Select FinBank from search list using XPATH
        mIdlingResource.waitForEvent( "GetInstitutionsSuccess");
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-search\"]/div/div/div[1]/div")).perform(webClick());

        // Try and simulate back button press to return to previous page
        mIdlingResource.waitForEvent("sign-in");
        onView(isRoot()).perform(ViewActions.pressBackUnconditionally());
    }

    @Test
    public void test04PopupWindowWithCancel() throws InterruptedException {

        Connect.start(InstrumentationRegistry.getContext(), "https://pick3pro.com/TestOpenWin.html", new TestEventHandler());

        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.ID, "openWinBtn")).perform(webClick());

        Thread.sleep(5000);
        onView(withId(R.id.popupCloseTextButton)).perform(click());
    }

    @Test
    public void test05PopupWindowWithBackButton() throws InterruptedException {

        Connect.start(InstrumentationRegistry.getContext(), "https://pick3pro.com/TestOpenWin.html", new TestEventHandler());

        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.ID, "openWinBtn")).perform(webClick());

        Thread.sleep(5000);
        onView(isRoot()).perform(ViewActions.pressBack());

        // Dismiss dialog with yes
        Thread.sleep(5000);
        onView(withId(android.R.id.button1)).perform(ViewActions.click());
    }

    /*
    @Test
    public void test04ConnectWithGoodUrlThenPrivacyPolicy() throws InterruptedException {

        Connect.start(InstrumentationRegistry.getContext(), goodUrl, new TestEventHandler());

        // Wait for Route search or let it timeout
        mIdlingResource.waitForEvent("search");
        onWebView()
            .withElement(findElement(Locator.NAME, "Search for your bank"))
            .perform(DriverAtoms.clearElement())
            .perform(DriverAtoms.webKeys("FinBank"))
            .perform(webClick());

        // Select FinBank from search list using XPATH
        mIdlingResource.waitForEvent( "GetInstitutionsSuccess");
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-search\"]/div/div/div[1]/div")).perform(webClick());

        // Click Next using XPATH
        mIdlingResource.waitForEvent("sign-in");
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Privacy policy")).perform(webClick());

        // Try to dismiss Privacy Policy popup
        Thread.sleep(5000);
        onView(withId(R.id.popupCloseTextButton)).perform(click());

        Thread.sleep(5000);
        onWebView(Matchers.allOf(isDisplayed(), isJavascriptEnabled()))
                .withElement(findElement(Locator.LINK_TEXT, "Exit")).perform(webClick());

        Thread.sleep(1000);
        onWebView(Matchers.allOf(isDisplayed(), isJavascriptEnabled()))
                .withElement(findElement(Locator.LINK_TEXT, "Yes")).perform(webClick());
    }

    @Test
    public void test05ConnectWithGoodUrlThenPrivacyPolicyThenBackButton() throws InterruptedException {

        Connect.start(InstrumentationRegistry.getContext(), goodUrl, new TestEventHandler());

        // Wait for Route search or let it timeout
        mIdlingResource.waitForEvent("search");
        onWebView()
                .withElement(findElement(Locator.NAME, "Search for your bank"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("FinBank"))
                .perform(webClick());

        // Select FinBank from search list using XPATH
        mIdlingResource.waitForEvent( "GetInstitutionsSuccess");
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-search\"]/div/div/div[1]/div")).perform(webClick());

        // Click Next using XPATH
        mIdlingResource.waitForEvent("sign-in");
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Privacy policy")).perform(webClick());

        // Perform back button press to dismiss popup and display dialog to user
        Thread.sleep(5000);
        onView(isRoot()).perform(ViewActions.pressBack());

        // Dismiss dialog with yes
        Thread.sleep(5000);
        onView(withId(android.R.id.button1)).perform(ViewActions.click());

        Thread.sleep(5000);
        onWebView(Matchers.allOf(isDisplayed(), isJavascriptEnabled()))
                .withElement(findElement(Locator.LINK_TEXT, "Exit")).perform(webClick());

        Thread.sleep(1000);
        onWebView(Matchers.allOf(isDisplayed(), isJavascriptEnabled()))
                .withElement(findElement(Locator.LINK_TEXT, "Yes")).perform(webClick());
    }
    */

    @Test
    public void test06ConnectWithGoodUrlThenAddBankAccount() {

        Connect.start(InstrumentationRegistry.getContext(), goodUrl, new TestEventHandler());

        // Wait for Route search or let it timeout
        mIdlingResource.waitForEvent("search");
        onWebView()
                .withElement(findElement(Locator.NAME, "Search for your bank"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("FinBank"))
                .perform(webClick());

        // Select FinBank from search list using XPATH
        mIdlingResource.waitForEvent( "GetInstitutionsSuccess");
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-search\"]/div/div/div[1]/div")).perform(webClick());

        // Click Next using XPATH
        mIdlingResource.waitForEvent("sign-in");
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"financial-sign-in\"]/div[2]/app-button/a/div")).perform(webClick());

        // Fill out UserId and Password and submit form
        mIdlingResource.waitForEvent("login");
        onWebView()
                .withElement(findElement(Locator.NAME, "Banking Userid"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("demo"));

        onWebView()
                .withElement(findElement(Locator.NAME, "Banking Password"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("go"));

        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-login\"]/form/app-button/a")).perform(webClick());

        // Select 1st account in list using XPATH
        mIdlingResource.waitForEvent("DiscoverAccountsSuccess");
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-select-accounts\"]/div[2]/app-account-list/div/div[1]/app-checkbox/label/div/div")).perform(webClick());

        // Scroll down to save button and click
        mIdlingResource.waitForEvent("loading");
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Save")).perform(webClick());

        // Click Submit button
        mIdlingResource.waitForEvent("review-accounts");
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Submit")).perform(webClick());
    }

     @Test
    public void test07ConnectWithExpiredUrlThenFinishActivity() throws InterruptedException {

        Connect.start(InstrumentationRegistry.getContext(), badExpiredUrl, new TestEventHandler());

        Thread.sleep(10000);
        Connect.finishCurrentActivity();
    }

    @Test
    public void test08FinishActivity() {

        // Try and finish a Activity that was never started
        try {
            Connect.finishCurrentActivity();
            fail("Should have thrown runtime exception");
        } catch(RuntimeException e) {
            //success
        }
    }

    // Tests 09, 10, 11 use deprecated TestEventListener
    @Test
    public void test09ConnectWithExpiredUrl() throws InterruptedException {
        Connect.start(InstrumentationRegistry.getContext(), badExpiredUrl, new TestEventListener());

        Thread.sleep(10000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Exit")).perform(webClick());
    }

    @Test
    public void test10ConnectWithGoodUrlThenCancel() throws InterruptedException {

        Connect.start(InstrumentationRegistry.getContext(), goodUrl, new TestEventListener());

        Thread.sleep(10000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Exit")).perform(webClick());

        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Yes")).perform(webClick());
    }

    @Test
    public void test11ConnectWithGoodUrlThenAddBankAccount() throws InterruptedException {

        Connect.start(InstrumentationRegistry.getContext(), goodUrl, new TestEventListener());

        Thread.sleep(10000);
        // Search for FinBank
        onWebView()
                .withElement(findElement(Locator.NAME, "Search for your bank"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("FinBank"))
                .perform(webClick());

        // Select FinBank from search list using XPATH
        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-search\"]/div/div/div[1]/div")).perform(webClick());

        // Click Continue using XPATH
        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"financial-sign-in\"]/div[2]/app-button/a/div")).perform(webClick());

        // Fill out UserId and Password
        Thread.sleep(5000);
        onWebView()
                .withElement(findElement(Locator.NAME, "Banking Userid"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("demo"));

        onWebView()
                .withElement(findElement(Locator.NAME, "Banking Password"))
                .perform(DriverAtoms.clearElement())
                .perform(DriverAtoms.webKeys("go"));

        // Click Sign In using XPATH
        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-login\"]/form/app-button/a")).perform(webClick());

        // Select 1st account in list using XPATH
        Thread.sleep(10000);
        //*[@id="institution-select-accounts"]/div[2]/app-account-list/div/div[1]/app-checkbox/label/div/div
        onWebView().withElement(findElement(Locator.XPATH, "//*[@id=\"institution-select-accounts\"]/div[2]/app-account-list/div/div[1]/app-checkbox/label/div/div")).perform(webClick());

        // Scroll down to save button and click
        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Save")).perform(webScrollIntoView()).perform(webClick());

        // Click Submit button
        Thread.sleep(5000);
        onWebView().withElement(findElement(Locator.LINK_TEXT, "Submit")).perform(webClick());
    }

    @Test
    public void test12AlreadyRunning() throws InterruptedException {

        Connect.start(InstrumentationRegistry.getContext(), badExpiredUrl, new TestEventHandler());
        Thread.sleep(5000);

        // Try and start a 2nd Activity
        try {
            Connect.start(InstrumentationRegistry.getContext(), badExpiredUrl, new TestEventHandler());
            fail("Should have thrown runtime exception");
        } catch(RuntimeException e) {
            //success
        }
    }

    @Test
    public void test13NullEventHandler() throws InterruptedException {

        Connect.start(InstrumentationRegistry.getContext(), badExpiredUrl, (EventHandler) null);
        Thread.sleep(5000);
    }

    public class TestEventHandler implements EventHandler {
        private static final String TAG = "TestEventHandler";

        @Override
        public void onLoaded() {
            Log.i(TAG, ">>> TestEventHandler: Received Loaded event");
        }

        @Override
        public void onDone(JSONObject doneEvent) {
            Log.i(TAG, ">>> TestEventHandler: Received Done event\n>>>>>> " + doneEvent.toString());
        }

        @Override
        public void onCancel() {
            Log.i(TAG, ">>> TestEventHandler: Received Cancel event");
         }

        @Override
        public void onError(JSONObject errorEvent) {
            Log.i(TAG, ">>> TestEventHandler: Received Error event\n>>>>>> " + errorEvent.toString());
        }

        @Override
        public void onRouteEvent(JSONObject routeEvent) {
            try {
                String screenVal = routeEvent.getString("screen");
                Log.i(TAG, ">>> TestEventHandler: Received Route event\nscreen: " + screenVal);
                mIdlingResource.checkEvent(screenVal);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUserEvent(JSONObject userEvent) {
            try {
                String action = userEvent.getString("action");
                Log.i(TAG, ">>> TestEventHandler: Received User event\naction: " + action);
                mIdlingResource.checkEvent(action);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class TestEventListener implements EventListener {
        private static final String TAG = "TestEventListener";

        @Override
        public void onLoaded() {
            Log.i(TAG, ">>> TestEventListener: Received Loaded event");
        }

        @Override
        public void onDone(JSONObject doneEvent) {
            Log.i(TAG, ">>> TestEventListener: Received Done event\n>>>>>> " + doneEvent.toString());
        }

        @Override
        public void onCancel() {
            Log.i(TAG, ">>> TestEventListener: Received Cancel event");
        }

        @Override
        public void onError(JSONObject errorEvent) {
            Log.i(TAG, ">>> TestEventListener: Received Error event\n>>>>>> " + errorEvent.toString());
        }
    }

    public static class WebEventIdlingResource implements IdlingResource {
        private long startTime;
        private long waitingTime;
        private String expectedEvent;
        private boolean waitingOnExpectedEvent;
        private ResourceCallback resourceCallback;
        private final List<String> events = new ArrayList<>();

        public WebEventIdlingResource() {
            this.startTime = System.currentTimeMillis();
            this.waitingTime = 0;
            this.expectedEvent = "";
            this.waitingOnExpectedEvent = false;
        }

        public void waitForEvent(String expectedEvent) {
            this.startTime = System.currentTimeMillis();
            this.waitingTime = 15000;
            this.expectedEvent = expectedEvent;
            this.waitingOnExpectedEvent = true;
            internalCheckEvent();
        }

        private void internalCheckEvent() {
            // System.out.println(">>> TestEventHandler - eventStr:" + eventStr + "queuedEvents:" + events.toString());
            if (waitingOnExpectedEvent && events.contains(expectedEvent)) {
                events.remove(expectedEvent);
                waitingOnExpectedEvent = false;
                expectedEvent = "";
                waitingTime = 0;
                resourceCallback.onTransitionToIdle();
            }
        }

        public void checkEvent(String event) {
            events.add(event);
            internalCheckEvent();
        }

        @Override
        public String getName() {
            return com.finicity.connect.sdk.ConnectActivityTest.class.getName() + ":" + waitingTime;
        }

        @Override
        public boolean isIdleNow() {
            long elapsed = System.currentTimeMillis() - startTime;
            boolean idle = (elapsed >= waitingTime);
            if (idle) {
                waitingOnExpectedEvent = false;
                expectedEvent = "";
                waitingTime = 0;
                resourceCallback.onTransitionToIdle();
            }
            return idle;
        }

        @Override
        public void registerIdleTransitionCallback(IdlingResource.ResourceCallback resourceCallback) {
            this.resourceCallback = resourceCallback;
        }
    }

}
