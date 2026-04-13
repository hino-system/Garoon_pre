# Architecture

## 概要

本プロジェクトは、業務系グループウェアを想定した Android アプリです。  
主な題材は以下です。

- スケジュール管理
- 空き時間確認
- 掲示板機能
- ローカルキャッシュ
- テスト
- GitHub Actions による自動化

Android アプリ側の設計・改善を主題としており、サーバーサイドは動作確認に必要な最小限の実装にとどめています。

---

## 採用方針

本プロジェクトでは、以下の方針で構成を整理しています。

- UI は Jetpack Compose
- 画面状態は ViewModel が管理
- 依存注入は Hilt
- ネットワークは Retrofit / OkHttp / Moshi
- 設定値や軽量状態は DataStore
- 業務データは Room
- 定期同期は WorkManager

特に、ローカルデータの責務を **DataStore と Room に分離**したことを重要な設計判断としています。

---

## モジュール構成
app
core
feature
sync