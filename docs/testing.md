# Testing

## テスト戦略

このプロジェクトでは、**対象ごとに最適な粒度でテストする**ことを方針にしています。  
すべてを UI テストへ寄せるのではなく、責務に応じて次のように分担しています。

- 純粋ロジック / ViewModel は unit test
- Room は DAO test
- Worker は worker test
- 画面接続確認は Compose UI smoke test
- CI では実行コストに応じて unit/build と instrumented を分離

目指しているのは、

1. 壊れた場所が分かりやすいこと
2. 速いテストを普段の確認に使えること
3. Android 固有の要素は専用テストで押さえること

です。

---

## 1. テストレイヤ別の考え方

### Unit Test

#### 主な対象

- ViewModel の状態遷移
- 入力検証
- イベント発火
- 純粋ロジック / utility

#### 目的

- UI を起動せずに画面ロジックを高速に確認する
- 認証なし・保存失敗などの分岐を小さく検証する
- 失敗原因を ViewModel / utility に絞り込みやすくする

#### 主な使用技術

- JUnit4
- kotlinx-coroutines-test
- MockK

### DAO Test

#### 主な対象

- Room DAO の query
- upsert / replace / delete
- 日付範囲やユーザー絞り込み

#### 目的

- ローカルキャッシュが期待どおり更新されるか確認する
- UI テストだけでは見えにくい永続層の仕様を独立して確認する

#### 主な使用技術

- Room testing
- Android instrumented test

### UI Test

#### 主な対象

- アプリ起動
- Compose / Hilt / test runner の接続確認

#### 目的

- テスト基盤が壊れていないことを確認する
- UI テストを追加していくための土台を維持する

#### 主な使用技術

- Compose UI Test
- Hilt Android Testing
- `createAndroidComposeRule`

### Worker Test

#### 主な対象

- `CoroutineWorker` の戻り値
- 成功 / retry 分岐

#### 目的

- 同期処理の責務を UI から切り離して検証する
- Worker 自体のビジネスロジックを単独で確認する

#### 主な使用技術

- WorkManager testing
- `TestListenableWorkerBuilder`
- MockK

---

## 2. 現在のテスト構成

### A. ViewModel / pure Kotlin の unit test

#### `feature:home:ui`

- `HomeMenuViewModelTest`
  - 週間予定の反映
  - ログイン情報がない場合の扱い
  - ダミー機能押下時のメッセージ

#### `feature:availability:ui`

- `AvailabilityViewModelTest`
  - 保存済み preference の復元
  - ユーザー一覧の読み込み
  - 選択ユーザーの追加 / 削除
  - 表示対象週の再構築

#### `feature:schedule:ui`

- `EditScheduleViewModelTest`
  - 詳細データからの初期値反映
  - 自分以外の予定編集拒否
  - 更新成功時の saved イベント

- `ScheduleListViewModelTest`
  - 週移動時に同じ曜日列を維持すること
  - 日付グルーピング結果の整合性

#### app module の pure logic

- `CalendarPlacementTest`
  - 時間帯が重なる予定を複数列へ割り付けるロジック

### B. Room DAO test

#### `feature:schedule:data`

- `ScheduleOccurrenceDaoTest`
  - `observeByDate`
  - ユーザー絞り込み
  - 期間絞り込み
  - 指定ユーザーのみ削除

- `ScheduleDetailDaoTest`
  - 未登録時は `null`
  - upsert 後に取得できること
  - 同一 ID の置き換え

### C. UI smoke test

#### `app`

- `HomeSmokeTest`
  - アプリ起動
  - Hilt test runner の接続
  - Compose テスト基盤の生存確認

### D. Worker test

#### `sync`

- `SyncTodaySchedulesWorkerTest`
  - Repository 成功時に `Result.success()`
  - 例外時に `Result.retry()`

---

## 3. この構成にしている理由

### ViewModel test を先に厚くしている理由

このアプリで最も変化しやすいのは、画面状態・入力検証・ユーザー操作に対する反応です。  
そこを高速に確認できるよう、まず ViewModel の test を厚めにしています。

### DAO test を分けている理由

schedule 一覧は Room を Single Source of Truth に近い形で使っているため、DAO の正しさが UI 表示の土台になります。  
そのため、永続化の仕様を ViewModel test に混ぜず、DAO 単独で検証しています。

### UI test を smoke から始めている理由

UI テストは価値が大きい一方で、実行コストも高めです。  
そのため現段階では、まず **「起動・Hilt・Compose テスト基盤が壊れていないか」** を確認する smoke test を置いています。

### Worker test を独立させている理由

バックグラウンド同期は UI とは失敗条件も責務も違います。  
UI から切り離して `doWork()` の戻り値を検証できる形にしておくと、同期戦略の改善がしやすくなります。

---

## 4. テストデータと test double の考え方

### Repository / SessionStore は MockK で差し替える

ViewModel test では repository や SessionStore を mock に置き換え、

- 正常系
- 認証なし
- 例外発生
- 保存成功 / 失敗

のような分岐を狙って確認します。

### Coroutine test を前提にする

ViewModel は coroutine を使うため、`MainDispatcherRule` を使って `Dispatchers.Main` を test dispatcher へ差し替えています。

### DAO test は in-memory Room に寄せる

永続化の仕様は実際の Room 挙動に近い形で確認したいため、mock ではなく DAO の実体で確かめます。

### Worker test は builder ベースで構築する

Worker は `TestListenableWorkerBuilder` で作成し、WorkManager 実行環境そのものではなく、Worker ロジックに焦点を当てています。

---

## 5. CI での実行方針

CI は実行コストに応じて 2 本に分けています。

### `android-ci.yml`

役割:

- app assemble
- ViewModel 中心の unit test
- 速い確認を pull request / push で回す

主な実行内容:

- `:app:assembleApiDebug`
- `:feature:schedule:ui:testDebugUnitTest`
- `:feature:home:ui:testDebugUnitTest`
- `:feature:availability:ui:testDebugUnitTest`

### `android-instrumented-tests.yml`

役割:

- emulator 上での instrumented test 実行
- Compose UI / DAO / Worker を含む Android 実機系テストの検証

主な実行内容:

- `:app:connectedApiDebugAndroidTest`
- `:feature:schedule:data:connectedDebugAndroidTest`
- `:sync:connectedDebugAndroidTest`

この分離により、毎回重いテストを全部回さずに、**速いフィードバックと Android 固有テストの両立**を狙っています。

---

## 6. ローカル実行コマンド

### build

```bash
./gradlew :app:assembleApiDebug