;;
;; example of an 'interactive' display.
;; click on the 'cherry' sprite to add a lollipop sprite at a random location
;; click on a lollipop sprite to remove sprite.
;;
;; In this example the sprites themselves just send events into a channel and let the owner (the app)
;; decide how to act on these events.
;;

(ns omreactpixi.examples.interactive
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.events :as events]
            [om.core :as om :include-macros true]
            [schema.core :as schema]
            [om-tools.core :refer-macros [defcomponentk]]
            [omreactpixi.abbrev :as pixi]
            [clojure.string :as string]
            [cljs.core.async :as async :refer [>! <! put!]]))

(defn assetpath [asset] (str "../assets/" asset))

(enable-console-print!)


;;
;; app state is width,height, and a vector of sprites
;; we support both adding and removing sprites
;;

(def appstate (atom {:width 100 :height 100 :sprites (sorted-map)}))

@appstate


(defn addrandomsprite
  "Returns a new app state containing an additional randomly-placed sprite."
  [appstate]
  (let [newx (rand-int (:width appstate))
        newy (rand-int (:height appstate))
        spriteid (str (gensym))
        newsprite {:x newx
                   :y newy
                   :key spriteid
                   :interactive true
                   }
        newsprites (assoc (:sprites appstate) spriteid newsprite)]
    (assoc appstate :sprites newsprites)))

(defn removespritebyid
  "Returns a new app state with the sprite (specified by id) removed"
  [appstate removeid]
  (update-in appstate [:sprites] #(dissoc % removeid)))

;;
;; om-react components are here
;;

(defcomponentk clickablesprite [[:data clickchannel spritedata] owner]
  (display-name [_] "ClickableSprite")
  (render [_] (let [spritecenter (PIXI.Point. 0.5 0.5)
                    clickfeed #(put! clickchannel spritedata)]
                (pixi/sprite (assoc spritedata :anchor spritecenter :click clickfeed)))))

(defcomponentk removeablesprites [[:data sprites deletechannel] owner]
  (display-name [_] "RemovableSprites")
  (render [_] (let [bindimageandchannel (fn [spr index] {:spritedata (assoc spr :image (assetpath "lollipopGreen.png"))
                                           :clickchannel deletechannel :key (:key spr)})]
                ;; this is still kind of yucky - fix up based on om-tools macro?
                (apply pixi/displayobjectcontainer
                       {:x 0 :y 0}
                       (om/build-all clickablesprite (vals sprites) {:key :key :fn bindimageandchannel})))))


(defcomponentk interactivestage [[:data width height sprites :as props ] owner]
  (display-name [_] "InteractiveStage")
  (init-state [_] {:addspritechannel (async/chan) :deletespritechannel (async/chan)})
  (did-mount [_]
             ;; run go blocks to add and remove sprites when the proper events come in
             (let [addchan (om/get-state owner :addspritechannel)
                   removechan (om/get-state owner :deletespritechannel)]
               (go (while true
                     (<! addchan)
                     (om/transact! props addrandomsprite)))
               (go (loop [spritedata (<! removechan)]
                     (om/transact! props (fn [state] (removespritebyid state (:key @spritedata))))
                     (recur (<! removechan))))))
  (render-state [_ state]
                (let [addchannel (:addspritechannel state)
                      deletechannel (:deletespritechannel state)
                      fontstyle {:font "40 Comic_Neue_Angular"}
                      fonttint 16rff88ff88]
                  (pixi/stage {:width width :height height :interactive true}
                              (pixi/bitmaptext {:text "Click the cherry to add a lollipop" :x 10 :y 10 :tint fonttint :style fontstyle :key "text1"})
                              (pixi/bitmaptext {:text "Click a lollipop to remove it" :x 10 :y 60 :tint fonttint :style fontstyle :key "text2"})
                              (om/build clickablesprite {:spritedata {:x 100 :y 150 :key "addbutton" :interactive true :image (assetpath "cherry.png")} :clickchannel (:addspritechannel state)})
                              (om/build removeablesprites {:sprites sprites :deletechannel (:deletespritechannel state)})))))

;;
;; entry/start code
;;

(defn startinteractiveapp [w h]
  (swap! appstate #(-> % (assoc :width w) (assoc :height h)))
  (om/root interactivestage appstate
           {:target (.getElementById js/document "my-app")}))

;; gotta load the bitmap font first or else pixi bombs out

(let [inset #(- % 16)
      w (-> js/window .-innerWidth inset)
      h (-> js/window .-innerHeight inset)
      fontloader (PIXI.BitmapFontLoader. (assetpath "comic_neue_angular_bold.fnt"))]
  (.on fontloader "loaded" #(startinteractiveapp w h))
  (.load fontloader))
