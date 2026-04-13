# Testing

## 概要

本プロジェクトでは、対象ごとにテスト手法を分けています。  
すべてを同じ種類のテストで確認するのではなく、責務に応じて分けることで、テストの意図と保守性を整理しています。

---

## テスト方針

### 1. Unit Test
対象:
- ViewModel
- 入力検証
- 状態遷移
- イベント処理

目的:
- 画面ロジックを高速に検証する
- UI に依存せず状態変化を確認する
- 失敗時の原因を切り分けやすくする

使用:
- JUnit4
- kotlinx-coroutines-test
- MockK

### 2. DB Test
対象:
- Room DAO
- query / insert / delete / replace の確認

目的:
- ローカルデータの保存・参照が期待通りか確認する
- 一覧・詳細・絞り込みの挙動を分離して検証する

使用:
- Room testing
- Android instrumented test

### 3. UI Test
対象:
- Compose UI
- 主要画面の起動確認

目的:
- 実際の画面起動と最低限の導線確認を行う
- Compose Test / Hilt / test runner の接続確認を行う

使用:
- Compose UI Test
- Hilt Android Testing

### 4. Worker Test
対象:
- WorkManager の Worker

目的:
- バックグラウンド処理の成功 / retry を確認する
- 同期処理の責務を UI と分けて検証する

使用:
- WorkManager testing
- MockK

---

## 現在導入しているテスト

### Unit Test

#### `ScheduleListViewModelTest`
確認内容:
- 初期ロードで一覧が反映される
- 認証ユーザー不在時にエラーになる

#### `HomeMenuViewModelTest`
確認内容:
- 週間予定の一覧反映
- 認証ユーザー不在時のエラー
- ダミーイベント押下時のメッセージ

#### `AvailabilityViewModelTest`
確認内容:
- 保存済み選択状態の反映
- ユーザー追加
- ユーザー削除

#### `EditScheduleViewModelTest`
確認内容:
- 詳細データの初期反映
- 自分以外の予定編集拒否
- 更新成功時の保存イベント

---

## DB Test

### `ScheduleOccurrenceDaoTest`
確認内容:
- insert / observeByDate
- ユーザー絞り込み
- 期間 + ユーザー絞り込み
- 対象ユーザーだけ削除

### `ScheduleDetailDaoTest`
確認内容:
- 未登録時は null
- upsert 後に取得できる
- 同じ ID の置き換え

---

## UI Test

### `HomeSmokeTest`
確認内容:
- アプリ起動
- Compose UI Test / Hilt test runner の動作確認

現状では、まずテスト基盤が正しく動作することの確認を目的に、最小限の smoke test を置いています。

---

## Worker Test

### `SyncTodaySchedulesWorkerTest`
確認内容:
- Repository 成功時に `Result.success()`
- Repository 失敗時に `Result.retry()`

---

## テストの役割分担

### ViewModel Test を先に厚くしている理由
ViewModel は画面状態・入力検証・イベント処理などを持つため、  
UI そのものを操作しなくても、ロジックの品質を高く維持しやすいからです。

### DAO Test を別にしている理由
Room の query は UI テストだけでは追いにくいため、  
データ保存と取得の正しさを独立して確認するためです。

### UI Test を最小から始めている理由
UI テストは実行コストが高いため、まずは smoke test を導入し、  
土台が安定してから対象画面を増やす方針にしています。

### Worker Test を分ける理由
バックグラウンド処理は ViewModel や UI と責務が異なるため、  
成功 / retry の挙動を単独で確認できるようにしています。

---

## テスト実行例

### Unit Test
./gradlew :feature:schedule:ui:testDebugUnitTest
./gradlew :feature:home:ui:testDebugUnitTest
./gradlew :feature:availability:ui:testDebugUnitTest