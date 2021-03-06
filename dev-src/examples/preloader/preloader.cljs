(ns omreactpixi.examples.preloader
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.events :as events]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [omreactpixi.core :as pixi :include-macros true]
            [clojure.string :as string]
            [cljs.core.async :as async :refer [>! <! put!]]))

;;
;; to add to the confusion, there are two pixi namespaces:
;; 1. 'pixi' contains react-pixi components usable with om
;; 2. 'js/PIXI' is the raw PIXI module

(defn assetpath [name] (str "../assets/" name))

(def preloadlist (map assetpath ["comic_neue_angular_bold.fnt" "creamVanilla.png" "creamChoco.png" "creamPink.png" "creamMocha.png"]))

(defonce appstate (atom {:text "argh!" :preloadmanifest preloadlist}))


(defn pixiloadchannel
  "Pass in a vector of graphics URI's to load. Add ':crossdomain true' if you want x-domain loading.
  Creates a PIXI AssetLoader and returns a channel. Each onProgress event contents are dumped
  into the channel (content is the relevant loader). The channel is closed when
  all assets are loaded (onComplete)."
  [preloadlist & {:keys [crossdomain] :or {crossdomain false}}]
  (let [statuschannel (async/chan)
        pixiloader (js/PIXI.AssetLoader. (into-array preloadlist) crossdomain)]
    (set! (.-onProgress pixiloader) #(put! statuschannel %))
    (set! (.-onComplete pixiloader) #(cljs.core.async/close! statuschannel))
    (.load pixiloader)
    statuschannel))

(defn load-then
  "Loads all the assets in the given preloader list and then calls the specified 'done function'"
  [preloadlist donefn]
  (let [loadingstatuschannel (pixiloadchannel preloadlist)]
    (go
     (loop [status (<! loadingstatuschannel)]
       #_(js/console.log status)
       (if-let [nextstatus (<! loadingstatuschannel)]
         (recur nextstatus)
         (donefn))))
    ))

(defn autostage
  "If the app contains a :preloadmanifest key, this component first displays the loading screen
  component and then, once all assets in the manifest are loaded, displays the main screen
  component."
  [app owner]
  (let [loadopts #js {:width 400 :height 300 :backgroundcolor 16rff00ff}
        mainopts #js {:width 400 :height 300 :backgroundcolor 16r00ffff}
        textopts #js {:x 100 :y 100 :key "ack" :text "Assets loaded!"}]
    (reify
      om/IInitState
      (init-state [_] {:preloading (contains? (om/get-props owner) :preloadmanifest)})
      om/IDidMount
      (did-mount [_] (when (contains? app :preloadmanifest)
                       (load-then (:preloadmanifest app) #(om/set-state! owner :preloading false))))
      om/IRenderState
      (render-state [_ state]
                    (if (:preloading state)
                      (pixi/stage loadopts (pixi/text {:x 10 :y 10 :text "Loading"}))
                      (pixi/stage mainopts (pixi/text textopts))))
      om/IDisplayName
      (display-name [_] "Autostage"))))


(defn startpreloader [appstate elementid]
  (om/root autostage appstate
           {:target (.getElementById js/document elementid)}))


(startpreloader appstate "my-app")
