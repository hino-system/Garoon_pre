# Garoon_pre

業務系グループウェアを想定した Android アプリのポートフォリオです。  
スケジュール管理、空き時間確認、掲示板機能を題材に、**モダン Android 開発での設計・実装・改善**を意識して作成しました。

単に画面を作るだけでなく、以下を重視しています。

- Jetpack Compose を用いた UI 実装
- マルチモジュール構成による責務分離
- DataStore と Room の役割分離
- WorkManager による同期
- Unit Test / DAO Test / UI Test / Worker Test の導入
- GitHub Actions による build / test 自動化

---

## このリポジトリで見てほしい点

- **UI 実装だけでなく、継続開発しやすい構成を意識していること**
- **ローカルデータの責務を DataStore と Room に分けていること**
- **ViewModel / DAO / UI / Worker でテスト対象を分けていること**
- **GitHub Actions で build / test を自動化していること**

詳細な設計判断は以下にまとめています。

- [Architecture](docs/architecture.md)
- [Decisions](docs/decisions.md)
- [Testing](docs/testing.md)
---

## アプリ概要

本アプリは、業務利用を想定したグループウェア系 Android アプリの試作です。  
主に Android アプリ側の設計・実装・改善を題材にしており、サーバーサイドは動作確認に必要な最小限の実装にとどめています。


- ログイン
- スケジュール一覧 / 詳細 / 作成 / 編集
- 空き時間確認
- 掲示板一覧 / 詳細 / 投稿 / コメント

---

## 主な機能

### スケジュール
- 予定一覧の表示
- 予定詳細の表示
- 予定作成 / 編集
- 繰り返し予定への対応

### 空き時間確認
- 複数ユーザーの予定比較
- 選択状態の保持
- 週単位での確認

### 掲示板
- 掲示板一覧表示
- 投稿一覧表示
- 投稿詳細表示
- 投稿作成 / 編集
- コメント投稿

### その他
- ログイン状態の保持
- 定期同期
- Compose ベースの UI

---

## 技術スタック

### Android / UI
- Kotlin
- Jetpack Compose
- Navigation Compose
- Material 3
- Single Activity

### アーキテクチャ / 状態管理
- MVVM
- Multi Module
- Hilt
- Kotlin Coroutines / Flow

### データ層
- Retrofit
- OkHttp
- Moshi
- Room
- DataStore
- WorkManager

### テスト
- JUnit4
- kotlinx-coroutines-test
- MockK
- Compose UI Test
- Room DAO Test
- Worker Test

### CI
- GitHub Actions

---

## アーキテクチャ

## モジュール構成

- `app`
- `core`
  - `core:common`
  - `core:designsystem`
  - `core:model`
  - `core:network`
  - `core:session`
- `feature`
  - `feature:auth`
  - `feature:availability`
  - `feature:board`
  - `feature:home`
  - `feature:schedule`
  - `feature:user`
- `sync`

## 設計方針

本プロジェクトでは、ローカルデータの責務を次のように分けています。

- **DataStore**  
  セッション情報、ログイン状態、画面の軽量な選択状態などの保存
- **Room**  
  スケジュールのような業務データの保存と参照

この方針により、設定値と業務データを分離し、ローカルキャッシュの役割を明確化しました。

### スケジュール機能の方針

スケジュール機能は、`Room` をローカルの source of truth とする構成に寄せています。

- 一覧系は Room の `Flow` を監視
- 同期処理で API 結果を Room に反映
- ViewModel は DB 監視 + 同期を組み合わせて UI を更新

これにより、責務が API 直読み中心の構成より整理され、オフライン時にも直近データを扱いやすくしています。

---

## 工夫した点

### 1. DataStore と Room の責務を分離したこと
当初は軽量な永続化中心でしたが、スケジュールのような業務データについては Room に移行し、設定値と業務データの役割を分離しました。

### 2. 旧 API 依存を減らし、observe / sync ベースへ整理したこと
スケジュール機能では、互換目的の古い取得 API を段階的に削減し、一覧・詳細ともにローカルキャッシュ前提の構成へ寄せました。

### 3. テストを層ごとに分けて導入したこと
以下のように、対象に応じてテスト手法を分けています。

- ViewModel のロジック  
  Unit Test
- Room DAO  
  Instrumented Test
- Compose UI  
  UI Test
- Worker  
  Worker Test

### 4. GitHub Actions による自動化を追加したこと
ローカル実行だけに依存せず、GitHub 上でも build / test を確認できるようにしました。

---

## テスト

現在は以下のテストを導入しています。

### Unit Test
- `ScheduleListViewModelTest`
- `HomeMenuViewModelTest`
- `AvailabilityViewModelTest`
- `EditScheduleViewModelTest`

### DB Test
- `ScheduleOccurrenceDaoTest`
- `ScheduleDetailDaoTest`

### UI Test
- `HomeSmokeTest`

### Worker Test
- `SyncTodaySchedulesWorkerTest`

### 今後の拡張
- UI テストの対象画面追加
- Repository 層のテスト強化
- スクリーンショットテスト導入
- テストケース数の拡充

---

## CI

GitHub Actions を用いて、以下の自動実行を設定しています。

### Android CI
- アプリのビルド
- Unit Test 実行

### Android Instrumented Tests
- emulator 上での instrumented test 実行

これにより、ローカル環境だけでなく、GitHub 上でも一定の品質確認ができる状態にしています。

---

## スクリーンショット

以下は主要画面の例です。  
`screenshots/` ディレクトリに画像を配置して参照してください。

## ログイン
![](screenshots/login.png)

## ホーム
![](screenshots/home.png)

## スケジュール一覧
![](screenshots/schedule_list.png)
![](screenshots/schedule_detail.png)

## スケジュール詳細
![](screenshots/schedule_detail.png)
![](screenshots/schedule_edit.png)


## 空き時間確認
![](screenshots/availability_list.png)
![](screenshots/availability_detail.png)

## 掲示板
![](screenshots/board_list_1.png)
![](screenshots/board_list_2.png)
![](screenshots/board_list_3.png)

---

## ディレクトリ構成
.
├─ .github/
│  └─ workflows/
├─ app/
├─ core/
├─ feature/
├─ sync/
├─ docs/
├─ screenshots/
├─ gradle/
├─ build.gradle.kts
├─ settings.gradle.kts
├─ README.md
└─ gradlew

## 開発環境

- Android Studio
- JDK 17
- compileSdk 35
- minSdk 26
- targetSdk 35