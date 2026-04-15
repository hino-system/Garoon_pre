# Architecture

## このドキュメントの目的

このプロジェクトは、**業務系グループウェアを題材にした Android アプリの設計ポートフォリオ**です。  
単に画面を作るのではなく、**継続開発しやすい構成・責務分離・テストしやすさ**を重視しています。

とくに意識しているのは、Android Developers が推奨している

- レイヤードアーキテクチャ
- Repository を入口にしたデータアクセス
- Single Source of Truth
- Unidirectional Data Flow
- ViewModel を中心にした UI 状態管理

という考え方です。

このリポジトリでは、それらを**ポートフォリオとして説明できる粒度まで落とし込むこと**を目的にしています。

---

## 1. 設計の前提

### スコープ

主題は Android アプリ側です。

- ログイン
- スケジュール一覧 / 詳細 / 作成 / 編集
- 空き時間確認
- 掲示板一覧 / 詳細 / 投稿 / コメント
- ローカルキャッシュ
- テスト
- GitHub Actions による自動化

### 非スコープ

サーバーサイドは本番品質の API ではなく、**Android 側の設計検証を支えるモック API**として扱っています。  
そのため、API は機能確認に必要な範囲へ絞り、責務の中心は Android 側に置いています。

---

## 2. アーキテクチャ全体像

### レイヤ構成

このプロジェクトでは、大きく次の 3 層で考えています。

1. **UI layer**  
   Compose 画面、ViewModel、画面イベント、画面状態
2. **Data layer**  
   Repository、Remote API、Room、DataStore
3. **Background / Integration layer**  
   WorkManager、アプリ起動時の Composition、依存注入

`domain` モジュールは、ビジネスロジックを大量に持つ use case 層というより、
**UI と data の間に置く契約層（Repository interface / domain model）**として使っています。

### 設計の意図

- UI は「表示とユーザー操作」に集中させる
- データ取得の入口は Repository に寄せる
- Remote / Local の詳細を ViewModel に漏らさない
- Room を使う対象と DataStore を使う対象を明確に分ける
- 同期処理を UI 操作から分離する

---

## 3. モジュール構成

```text
app
├─ navigation / app composition
│
├─ core
│  ├─ common         # 日付計算や共通ユーティリティ
│  ├─ designsystem   # Compose theme / UI 共通基盤
│  ├─ model          # 共通 model
│  ├─ network        # Retrofit / OkHttp / Moshi / Network DI
│  └─ session        # SessionStore(DataStore)
│
├─ feature
│  ├─ auth
│  │  ├─ domain
│  │  ├─ data
│  │  └─ ui
│  ├─ schedule
│  │  ├─ domain
│  │  ├─ data
│  │  └─ ui
│  ├─ availability
│  │  ├─ domain
│  │  ├─ data
│  │  └─ ui
│  ├─ board
│  │  ├─ domain
│  │  ├─ data
│  │  └─ ui
│  ├─ home
│  │  └─ ui
│  └─ user
│     └─ data
│
└─ sync
   └─ WorkManager Worker