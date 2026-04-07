
-----
# VehicleBuilder

**Next-Generation Vehicle Plugin for Minecraft (Paper 1.21.4)**

`VehicleBuilder`は、リソースパックを一切使用せず、バニラ環境のプレイヤー全員がハイクオリティな乗り物を体験できる、Display Entityベースの車両システムです。

-----
## 🚀 主な特徴 (Features)

* **100% Resource Pack Free:** サーバーリソースパックの強制ダウンロードは不要です。Display Entityを駆使し、バニラ環境でリアルな造形を再現します。
* **3D Greedy Meshing Optimization:** 建築物（ブロック）から車両を生成する際、独自のアルゴリズムでポリゴン数を劇的に削減。描画負荷を最小限に抑えます。
* **Packet-Level Optimization:** エンティティの移動パケットを最適化し、同時稼働時のサーバー負荷（MSPT）とネットワーク帯域を最小化しています。
* **Cross-Vehicle Support:** 自動車、飛行機、鉄道など、多様な移動手段を一つのプラグインで実現（順次実装予定）。
* **Developer Friendly API:** 他のプラグイン（経済システム等）との連携が容易な設計を目指しています。

## 📸 スクリーンショット (Screenshots)
![プロトタイプの画像](https://pixiv.pximg.net/fanbox/public/images/creator/45778676/profile/lwZcA17FxymLTzegDOiUJZTM.jpeg)

## 🛠 動作環境 (Requirements)

* **Minecraft Version:** 1.21.4 (Java Edition)
* **Server Software:** [Paper](https://papermc.io/) またはその派生 (NMSを使用しているため)
* **Java Version:** 21以上

## 📥 インストール (Installation)
現在開発中のため、リリースセクションから最新のベータ版（開発者向け）をダウンロードするか、ソースコードからビルドしてください。

1.  `plugins/` フォルダに `VehicleBuilder-<Version>.jar` を配置。
2.  サーバーを起動。
3.  `/vbmenu` コマンドでGUIを起動（詳細はWikiを参照予定）。

## 💖 支援のお願い (Support the Development)
このプロジェクトは、開発者のSrainが個人で開発しているオープンソースプロジェクトです。
現在の開発PC（Ryzen 5 3500）のスペック不足が課題となっており、よりスムーズな開発と検証のために機材アップグレードを目標としています。

もしこのプロジェクトに価値を感じていただけたら、**FANBOX**でのご支援をお願いいたします！

👉 [**Srain's FANBOX (Support Here)**](https://srain.fanbox.cc/)

-----

## 📄 ライセンス (License)
このプロジェクトは **GNU General Public License v3.0** の下で公開されています。
詳細は [LICENSE](./LICENSE) ファイルを参照してください。

-----
