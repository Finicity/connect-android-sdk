# Changelog

### 2.0.0 (May 27, 2021)

Patch:
- Updated EventHandler interface to match other SDKs.
- Removed deprecated EventListener interface.  Use EventHandler interface to receive Connect events.

### 1.0.6 (May 21, 2021)

Patch:
- Send SDK version and platform type to Connect.

### 1.0.5 (April 22, 2021)

Patch:
- Added Chrome custom tab support for displaying Oauth popup.

### 1.0.4 (September 22, 2020)

Patch:
- Updated user agent for Oauth popup to prevent webview from being blocked.
- Set versionCode to 104 and versionName to "1.0.4" in Android manifest.

### 1.0.2 (August 19, 2020)

Patch:
- Updated Connect.onCreate() to first call super.onCreate() before doing the EventListener / process restart check (see last change).  Turns out finishing an activity before calling super.onCreate() causes an exception.

### 1.0.1 (Summer 2020)

Patch:
- Added logic to Connect.onCreate() to immediately finish if it detects that Android has restarted the process.  This is because the static EventListener reference in Connect no longer exists and must be supplied by the parent activity.
