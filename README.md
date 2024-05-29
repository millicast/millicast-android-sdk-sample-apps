# Getting Started with Dolby.io Streaming Android SDK

This repository contains the starter sample app for Dolby.io [Getting Started Guide for Android](https://docs.dolby.io/streaming-apis/docs/android).

## Overview

This app uses an MVI pattern with Jetpack Compose to demonstrate using the Dolby.io Streaming Android SDK. It shows both subscribing to a multiview stream and publishing with the device's microphone and camera.

## Requirements

You will need:
- A [Dolby.io Account](https://dashboard.dolby.io/signup)
- Android Studio

### Publishing Tokens

For publishing to work:
- Use the Dolby.io Account to create or copy your publishing token
- Create a token.properties like this
```
PUBLISH_TOKEN="abcdefg12345"
STREAM_NAME="testAndroidStream"
```
- Add that file to the root of this project
- Sync gradle

## Give Feedback or Report a Bug

If you run into any errors or have questions, create a [GitHub issue](https://github.com/millicast/millicast-android-sdk-sample-apps/issues).


# About Dolby.io

Using decades of Dolby's research in sight and sound technology, Dolby.io provides APIs to integrate real-time streaming, voice & video communications, and file-based media processing into your applications. [Sign up for a free account](https://dashboard.dolby.io/signup/) to get started building the next generation of immersive, interactive, and social apps.

<div align="center">
  <a href="https://dolby.io/" target="_blank"><img src="https://img.shields.io/badge/Dolby.io-0A0A0A?style=for-the-badge&logo=dolby&logoColor=white"/></a>
&nbsp; &nbsp; &nbsp;
  <a href="https://docs.dolby.io/" target="_blank"><img src="https://img.shields.io/badge/Dolby.io-Docs-0A0A0A?style=for-the-badge&logoColor=white"/></a>
&nbsp; &nbsp; &nbsp;
  <a href="https://dolby.io/blog/category/developer/" target="_blank"><img src="https://img.shields.io/badge/Dolby.io-Blog-0A0A0A?style=for-the-badge&logoColor=white"/></a>
</div>

<div align="center">
&nbsp; &nbsp; &nbsp;
  <a href="https://youtube.com/@dolbyio" target="_blank"><img src="https://img.shields.io/badge/YouTube-red?style=flat-square&logo=youtube&logoColor=white" alt="Dolby.io on YouTube"/></a>
&nbsp; &nbsp; &nbsp; 
  <a href="https://twitter.com/dolbyio" target="_blank"><img src="https://img.shields.io/badge/Twitter-blue?style=flat-square&logo=twitter&logoColor=white" alt="Dolby.io on Twitter"/></a>
&nbsp; &nbsp; &nbsp;
  <a href="https://www.linkedin.com/company/dolbyio/" target="_blank"><img src="https://img.shields.io/badge/LinkedIn-0077B5?style=flat-square&logo=linkedin&logoColor=white" alt="Dolby.io on LinkedIn"/></a>
</div>
