# Decisions

## このドキュメントの位置づけ

ここでは、実装の羅列ではなく、**設計判断の理由とトレードオフ**をまとめます。  
ポートフォリオとして重要なのは「何を使ったか」よりも、**なぜその選択をしたか**を説明できることだと考えています。

---

## 1. UI に Jetpack Compose を採用した

### 判断

UI 実装は Jetpack Compose を前提に構成しました。

### なぜそうしたか

- 現在の Android で中心的な宣言的 UI であること
- state と UI の対応関係を読みやすくできること
- ViewModel と組み合わせた状態駆動設計と相性が良いこと
- Compose UI Test を含めた検証基盤へつなげやすいこと

### トレードオフ

- XML ベース資産の再利用には向かない
- チーム全体で Compose の設計知識が必要になる

### このプロジェクトでの扱い

Screen は表示責務へ寄せ、状態・イベント処理は ViewModel に置く形を基本にしています。

---

## 2. Single Activity + Navigation Compose を採用した

### 判断

複数 Activity を増やさず、Single Activity + Navigation Compose で画面遷移を構成しました。

### なぜそうしたか

- Compose 時代の標準的な構成に寄せたかった
- ルーティングの責務を `app/navigation` に集約したかった
- feature を増やしても遷移の入口を整理しやすいから

### トレードオフ

- Navigation graph が肥大化しやすい
- deep link や複雑な nested graph が増えると整理ルールが必要になる

### 補足

現状の規模では Single Activity のほうが説明と保守のバランスが良いと判断しています。

---

## 3. feature 単位のマルチモジュール構成にした

### 判断

`app / core / feature / sync` を基本単位にし、feature は `ui / domain / data` へ分割しました。

### なぜそうしたか

- 関心事を feature 単位で追いやすくしたかった
- UI / data source / 契約を分けたかった
- テスト対象を切り分けやすくしたかった
- 「画面は作れる」だけでなく「継続開発前提で整理できる」ことを示したかった

### トレードオフ

- 小規模アプリにはやや大きめの構成になる
- Gradle 設定や依存管理の説明コストが上がる

### この構成で得たこと

- schedule / board / availability を独立した関心事として扱いやすくなった
- Repository interface を境界にして UI テストや unit test が書きやすくなった

---

## 4. domain 層は「契約層」として薄く保った

### 判断

`domain` モジュールは、主に repository interface と domain model を置く層として使っています。  
use case はまだ本格導入していません。

### なぜそうしたか

- 現時点では複数 ViewModel で共有する複雑なビジネスロジックがまだ多くない
- 先に責務境界だけを作り、必要になった時点で use case を足すほうが自然だから
- 「Clean Architecture を形だけ増やす」より、現状に必要な厚みへ留めたかったから

### トレードオフ

- ViewModel に寄るロジックが増えると膨らみやすい
- 今後の複雑化に対しては追加分割が必要になる

### 今後の判断基準

- 複数画面で同じロジックを共有し始めた時
- ViewModel が data orchestration を持ちすぎた時

このあたりが use case 導入のタイミングです。

---

## 5. Repository をデータアクセスの入口に統一した

### 判断

ViewModel は API / DAO / DataStore を直接触らず、Repository interface 経由でデータへアクセスします。

### なぜそうしたか

- Android の推奨構成に沿って data layer を明確にしたかった
- Remote / Local の切り替えを UI に漏らしたくなかった
- test double を差し込みやすくしたかった

### トレードオフ

- 小さな機能でも interface / impl の分離が必要になる
- 一見遠回りに見えるコードが増える

### このプロジェクトでの具体例

- `ScheduleRepository` は Room と API の両方を内包する
- `BoardRepository` は現状 network-first の窓口になる
- `AuthRepository` はログイン後の SessionStore 更新まで責務に含める

---

## 6. DataStore を session / 軽量状態の保存先にした

### 判断

ログイン状態や軽量な画面選択状態は `SessionStore(DataStore)` に集約しました。

### なぜそうしたか

- token やログインユーザー情報は key-value で十分だから
- Flow で扱いやすく、ViewModel と相性が良いから
- SharedPreferences より責務を整理しやすいから

### 保存しているもの

- API token
- auth user id / user id
- 表示名 / 部署 / 役職
- 空き時間確認の選択状態

### トレードオフ

- 複雑な検索・一覧・範囲取得には向かない
- 業務データのキャッシュを何でも入れると責務が崩れる

---

## 7. Room は schedule 系だけに導入した

### 判断

Room は schedule 一覧 / 詳細キャッシュのために導入し、board にはまだ入れていません。

### なぜそうしたか

- schedule は日付単位・週単位・詳細単位の読み分けがある
- 一覧画面で observe ベースの更新を成立させたかった
- cache backed UI を作る題材として schedule が最も適していた

### なぜ board に入れていないか

- まずは API 契約と UI / 編集フローを優先したかった
- ローカル DB を導入しなくても機能責務は十分説明できるから
- 全 feature を同じ重さで作るより、必要なところから厚くしたかったから

### トレードオフ

- board は offline 性や再表示速度の改善余地が残る
- データ戦略が feature ごとに異なるため、説明は必要になる

---

## 8. schedule 一覧は observe と sync を分離した

### 判断

`ScheduleListViewModel` / `HomeMenuViewModel` / `AvailabilityViewModel` では、

- 先に Room を observe し
- その後 API 同期を走らせる

という構成にしています。

### なぜそうしたか

- UI が remote fetch の完了だけを待つ構造にしたくなかった
- Room を Single Source of Truth に近い形で使いたかった
- 同期結果を Flow 更新で自然に UI へ反映したかった

### トレードオフ

- 実装が単純な `fetch -> setState` より少し複雑になる
- 一覧表示の責務を DAO / mapper / repository に分ける必要がある

### 得られたもの

- schedule 画面の読み込み構造を説明しやすい
- DAO テストと ViewModel テストの責務を分けやすい

---

## 9. WorkManager は「補助同期」に限定した

### 判断

WorkManager は `SyncTodaySchedulesWorker` として導入し、まずは当日予定の同期に絞りました。

### なぜそうしたか

- バックグラウンド処理の責務を UI から分離したかった
- Worker をテスト対象として独立させたかった
- いきなり大きな同期基盤を作るより、まずは小さく価値を示したかった

### トレードオフ

- 現状ではフル同期戦略まではカバーしていない
- 実運用寄りにするなら constraints / unique work / backoff 設計がまだ必要

### この判断の意味

「WorkManager を使っています」で終わらず、  
**どの責務を Worker に切り出したか**まで説明できる構成にしています。

---

## 10. availability preference に adapter 形を用意した

### 判断

LOCAL / API / AWS / AZURE / GCP を見据えた adapter 形を feature に持たせました。

### なぜそうしたか

- 設定保存の責務を schedule 本体から分離したかった
- 永続化方式を差し替え可能な形で考えていることを示したかった
- ポートフォリオとして、単一実装に閉じない設計意図を見せたかった

### トレードオフ

- 現時点では構想先行の部分があり、すべてが実装済みではない
- 実際の build variant / DI 切り替えは今後の改善対象

### ポリシー

未完成の部分は「完成したことにしない」。  
準備済みの構造と、実際に使っている実装を分けて説明する方針です。

---

## 11. Hilt を全面採用した

### 判断

ViewModel / Repository / Retrofit / Room / Worker まで Hilt で配線しています。

### なぜそうしたか

- 依存関係を明示化したかった
- test 時の差し替えやモジュール境界を整理しやすいから
- Android で一般的な DI 構成として説明しやすいから

### トレードオフ

- 学習コストと annotation ベースの設定量は増える
- 小規模機能だけ見ると大げさに見えることもある

### 得られたこと

- Network / Repository / Worker まで一貫した依存注入方針を取れた
- Hilt Android Test / Worker Test にもつなげやすくなった

---

## 12. テストは「対象ごとに分ける」方針にした

### 判断

Unit Test / DAO Test / UI Test / Worker Test を役割別に分けています。

### なぜそうしたか

- ViewModel と DAO と UI では確認したい責務が違うから
- 速いテストを中心に積み上げたかったから
- Room や Worker は専用のテスト方法のほうが明確だから

### トレードオフ

- テスト基盤が複数になる
- 実行方法や失敗時の見方をチームでそろえる必要がある

### それでも分ける理由

「全部 UI テストで見る」より、  
**どこで壊れたかを切り分けやすい構造**のほうが継続開発向きだからです。

---

## 13. GitHub Actions は unit/build と instrumented を分けた

### 判断

CI は 1 本に詰め込まず、

- build + unit test
- emulator を使う instrumented test

を分けて扱います。

### なぜそうしたか

- 実行コストが異なるから
- 速い確認を先に回したいから
- Android の UI / DAO / Worker テストはエミュレータ依存になるため、単純な unit test と分けたほうが運用しやすいから

### トレードオフ

- workflow が複数になる
- どのテストがどこで実行されるかを README / docs で説明する必要がある

---

## 14. モック API は Android 本体から分離した

### 判断

動作確認用の Node.js mock API は別リポジトリとして整理します。

### なぜそうしたか

- Android アプリの責務とサーバーの責務を分けたいから
- Android 側を読みたい人が backend 実装に埋もれないようにしたいから
- server 側だけ単独で起動・修正・公開しやすくしたいから

### トレードオフ

- リポジトリが増える
- API 契約の同期を README などで保つ必要がある

### 期待する効果

- Android ポートフォリオとしての焦点がぶれない
- mock API も GitHub に上げやすくなる

---

## 15. 未完了部分を明示する方針を取った

### 判断

README や docs では、実装済み・改善中・今後の課題を意図的に分けて書きます。

### なぜそうしたか

- 実装していないものまで「対応済み」と見せたくないから
- 設計判断だけでなく、改善の途中経過も成果物だと考えているから
- 面接やレビューで会話しやすくなるから

### このプロジェクトでの具体例

- board は network-first であることを隠さない
- availability backend abstraction は発展途上であることを明示する
- sync / error handling / offline 方針には拡張余地があることを書く

---

## まとめ

このプロジェクトで特に重要な判断は次の 5 つです。

1. **Compose + ViewModel で状態駆動 UI を作る**
2. **feature 単位のマルチモジュールで責務を分ける**
3. **Repository をデータアクセスの入口に統一する**
4. **DataStore と Room の用途を明確に分離する**
5. **テストと CI を継続開発前提で設計する**

ポートフォリオとして見せたいのは、単に「モダン技術を使ったこと」ではなく、  
**技術選定・責務分離・未完了部分の扱いまで含めて判断できること**です。