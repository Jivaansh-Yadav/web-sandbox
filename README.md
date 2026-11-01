<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:0a0f14,55:1e2933,100:94a3b8&height=235&section=header&text=Web%20Sandbox&fontSize=46&fontColor=f1f5f9&fontAlignY=40&desc=Any%20URL%20as%20a%20native%20app%2C%20with%20no%20address%20bar&descSize=17&descAlignY=62&animation=twinkling" alt="Web Sandbox" width="100%">

<img src="https://readme-typing-svg.demolab.com?font=JetBrains+Mono&weight=500&size=19&duration=3200&pause=900&color=cbd5e1&center=true&vCenter=true&width=680&lines=A%20profile%20is%20a%20URL%20plus%20injections;Hide%20the%20ads%2C%20keep%20the%20app%2C%20never%20show%20Chrome;Leave%20with%20three%20fingers%20for%201.3%20seconds" alt="Typing animation" />

<br />

<img src="https://img.shields.io/badge/Platform-Android-3ddc84?style=flat-square&labelColor=0a0f14" alt="Android" />
<img src="https://img.shields.io/badge/Kotlin-%2B%20Compose%20M3-cbd5e1?style=flat-square&labelColor=0a0f14" alt="Kotlin Compose Material 3" />
<img src="https://img.shields.io/badge/Storage-Room-94a3b8?style=flat-square&labelColor=0a0f14" alt="Room" />
<img src="https://img.shields.io/badge/Tests-Robolectric%20%2B%20Roborazzi-64748b?style=flat-square&labelColor=0a0f14" alt="Robolectric and Roborazzi" />

</div>

<br />

<div align="center">

<sub>An Android shell that turns a website into a full-screen app — and lets you delete the parts of that website you did not want.</sub>

</div>

---

<div align="center">

## What it is

</div>

A sandbox browser for one site at a time. You create a **profile**: a name, a URL, and a set of injections. Launch it and the site fills the screen edge-to-edge — no address bar, no tabs, no Chrome chrome, no white flash on load. To the person using it, it is an app.

The injections are the point. Per profile you can add **custom CSS**, a list of **selectors to hide**, and **custom JavaScript**, and the shell re-applies them as the page mutates. That is what makes a mobile web app feel owned rather than visited: remove the cookie banner, remove the "open in app" nag, hide the nav that duplicates the OS back gesture.

And because a full-screen shell with no browser controls is a trap, escape is a gesture you would never trigger by accident.

<div align="center">

## The profiles

</div>

A profile is one row in Room, and everything below is per-site behaviour rather than a global setting:

<ul>
<li><b><code>name</code> · <code>url</code></b> — what it is, where it goes.</li>
<li><b><code>isDesktopMode</code></b> — spoofs a desktop Chrome user agent, for sites that refuse to render properly on mobile.</li>
<li><b><code>customCss</code></b> — appended as a style block on every load and re-injection.</li>
<li><b><code>cssHideSelectors</code></b> — a selector list turned into <code>display: none</code> rules, watched by a <code>MutationObserver</code>.</li>
<li><b><code>customJs</code></b> — arbitrary script, run inside the page after the injection package.</li>
<li><b><code>blockExternalPages</code></b> — domain lockdown, on by default.</li>
<li><b><code>isPullToRefreshEnabled</code></b> — refresh the site without a browser chrome reload.</li>
<li><b><code>isDirectLaunch</code></b> — skip the dashboard and open straight into this profile.</li>
</ul>

<div align="center">

## What is inside

</div>

<ul>
<li><b>Domain lockdown</b> — a navigation is blocked unless the destination host and the profile's host contain each other. Off-domain links are intercepted with a toast, not silently loaded; <code>tel:</code>, <code>mailto:</code> and <code>sms:</code> are handed to the OS instead, because those are not web pages.</li>
<li><b>Injection that survives re-renders</b> — a single combined script builds the style block, applies the hide rules, and installs a <code>MutationObserver</code> that re-hides anything the site re-adds.</li>
<li><b>Two escape gestures</b> — <b>three fingers held for 1.3 s</b>, or <b>five taps within 450 ms of each other</b>. Both open an exit dialog; neither can be triggered by scrolling or typing.</li>
<li><b>Edge-to-edge, dark under the hood</b> — the container is black so a slow site never flashes white, and a loading overlay fades out.</li>
<li><b>Profiles in Room</b> — DAO, database, repository, ViewModel; edits persist and the dashboard lists them.</li>
<li><b>Material 3</b> — Compose with a real theme (<code>Color</code>, <code>Type</code>, <code>Theme</code>), not a scaffold left at defaults.</li>
<li><b>Tests that run on a JVM</b> — Robolectric for behaviour, Roborazzi for screenshots, no emulator in CI.</li>
</ul>

---

<div align="center">

## Stack

</div>

<ul>
<li><b>Kotlin + Jetpack Compose</b>, Compose BOM <code>2024.09.00</code>, Material 3.</li>
<li><b>Room 2.7.0</b> — KSP-generated DAO; profiles are the only persisted state.</li>
<li><b>WebView</b> — with <code>WebViewClient</code> overrides for the UA string, the injection pipeline and the lockdown check.</li>
<li><b>Robolectric 4.16.1 + Roborazzi 1.59.0</b> — JVM unit tests and screenshot tests.</li>
<li><b>Version catalog + Gradle wrapper</b> — versions in <code>gradle/libs.versions.toml</code>, and a committed <code>gradlew</code> so CI does not depend on a local install.</li>
</ul>

---

<div align="center">

## Under the hood

</div>

<details>
<summary><b>Why a MutationObserver, and not just a style tag</b></summary>

<br />

<p>Injecting <code>display: none</code> once works until the site re-renders — and modern sites re-render constantly. A framework re-creates the element it removed a second ago, and the banner comes back.</p>

<p>So the hide rules are also applied by an observer that runs on every mutation:</p>

<pre><code>if (window.MutationObserver) {
  var obs = new MutationObserver(hideAll);
  obs.observe(document.documentElement, { childList: true, subtree: true });
}</code></pre>

<p>Everything the shell injects is assembled into one script string and evaluated once per page load — CSS, hide rules, observer, then custom JS — so a single <code>evaluateJavascript</code> call does the whole job and custom JS sees a page that is already cleaned up.</p>

</details>

<details>
<summary><b>Domain lockdown is bidirectional on purpose</b></summary>

<br />

<pre><code>val isInternal = host.contains(initialHost) || initialHost.contains(host)
if (profile.blockExternalPages &amp;&amp; initialHost.isNotEmpty() &amp;&amp; !isInternal) {
  Toast.makeText(context, "External navigation blocked inside Sandbox", Toast.LENGTH_SHORT).show()
  return true   // intercept and block
}</code></pre>

<p>Both directions matter. <code>host.contains(initialHost)</code> allows <code>app.example.com</code> when the profile is <code>example.com</code> — subdomains are part of the same site. <code>initialHost.contains(host)</code> allows the reverse, which is what makes logins and redirects work when a site bounces between a host and its parent domain.</p>

<p>It is a containment test, not an equality test, and it is a lockdown rather than a security boundary: the goal is "the user does not accidentally wander into a full-screen page they cannot escape", which is why non-HTTP schemes are forwarded to the OS instead of being blocked.</p>

</details>

<details>
<summary><b>Escape gestures you cannot trip over</b></summary>

<br />

<p>A full-screen shell with the browser controls gone has one requirement above all others: <b>a way out that a pocket, a scroll and a two-thumbed game cannot trigger</b>. Two are provided, and both are deliberate:</p>

<ul>
<li><b>Five taps, each within 450 ms of the last.</b> A pause resets the counter to one, so slow tapping never accumulates.</li>
<li><b>Three simultaneous touches, all held for 1.3 s.</b> The loop exits early the instant any contact lifts — a three-finger scroll cancels itself.</li>
</ul>

<p>Both set the same <code>showExitDialog</code> flag. Neither is discoverable, so a hint is shown on the dashboard and inside the exit dialog: an escape hatch nobody knows about is not an escape hatch.</p>

</details>

<details>
<summary><b>Desktop mode is one string</b></summary>

<br />

<pre><code>userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                  "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"</code></pre>

<p>Some sites serve a deliberately broken page to mobile UAs — a "download our app" wall, or a layout with no working nav. Switching the UA is a blunt instrument that solves most of them, and it is per profile because it should never be global: the mobile site is usually the better one.</p>

</details>

<details>
<summary><b>Testing a WebView app without an emulator</b></summary>

<br />

<p>Robolectric runs Android framework code on the JVM, so the profile model, the DAO and the screen logic are unit-testable in CI. Roborazzi takes screenshots in the same run, which is what catches the class of failure this app is most exposed to: a layout that only looks right on one screen size.</p>

<p>The instrumented test is still there for the parts that genuinely need a device. The distinction matters — screenshot tests are only useful if they run on every push.</p>

</details>

---

<div align="center">

## Project structure

</div>

<pre><code>web-sandbox/
├── app/src/main/java/com/example/
│   ├── MainActivity.kt
│   ├── data/                  # SandboxProfile (Room entity) · Dao · Database · Repository
│   └── ui/
│       ├── SandboxDashboard.kt    # profile list, editor, gesture hint
│       ├── SandboxViewModel.kt
│       ├── SandboxWebView.kt      # UA · injections · lockdown · escape gestures
│       └── theme/                 # Color · Type · Theme
├── app/src/test/              # Robolectric + Roborazzi screenshot test
├── gradle/libs.versions.toml
└── gradlew</code></pre>

---

<div align="center">

## Build it

</div>

<pre><code>git clone https://github.com/Jivaansh-Yadav/web-sandbox.git
cd web-sandbox
./gradlew assembleDebug        # APK in app/build/outputs/apk/debug/
./gradlew test                 # Robolectric + Roborazzi on the JVM</code></pre>

<p>Requires the Android SDK and JDK 17. There is no backend, no API key and no account — profiles are local rows in Room.</p>

<div align="center">

<sub>The AI Studio export of this same app lives in the private <b>web-sandbox-gai</b> repository; this is the one with the Gradle wrapper and the CI-shaped tests.</sub>

</div>

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:0a0f14,55:1e2933,100:94a3b8&height=110&section=footer&reversal=true" alt="" width="100%">
