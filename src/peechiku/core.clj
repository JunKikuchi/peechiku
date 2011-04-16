(ns peechiku.core
  (:use
    clojure.contrib.json
    compojure.core
    ring.util.response
    ring.middleware.keyword-params
    ring.middleware.nested-params
    ring.middleware.params
    hiccup.core
    hiccup.page-helpers
    appengine-magic.services.user)
  (:require
    [compojure.route :as route]
    [appengine-magic.core :as ae]
    [appengine-magic.services.channel :as channel]
    [appengine-magic.services.datastore :as ds])
  (:import
    [com.google.appengine.api.channel ChannelFailureException]))

(defn index [] {
  :status 200
  :headers {"Content-Type" "text/html"}
  :body (
    html4
    [:head
      [:title "peechiku"]
      (include-css "http://yui.yahooapis.com/combo?3.3.0/build/cssreset/reset-min.css&3.3.0/build/cssfonts/fonts-min.css&3.3.0/build/cssgrids/grids-min.css")
      (include-css "http://yui.yahooapis.com/3.3.0/build/cssbase/base-min.css")
      (include-css "/static/base.css")
      (include-js "http://ajax.googleapis.com/ajax/libs/jquery/1.5.1/jquery.min.js")
      (include-js "/_ah/channel/jsapi")
      (include-js "/static/base.js")
    ]
    [:body
      [:div.yui3-g
        [:div#header.yui3-u-1
          [:div
            [:span#user (. (current-user) toString)]
            [:span#signout [:a {:href (logout-url)} "logout"]]
          ]
          [:h1 [:a {:href "/"} "peechiku"]]
        ]
        [:div#content.yui3-u-1
          [:form
            [:input#text {:type "text" :name "text" :value ""}]
            [:button#send {:name "send"} "send"]
          ]
          [:div#log]
        ]
        [:div#footer.yui3-u-1 [:p "&copy; 2011 Jun Kikuchi"]]
      ]
    ]
  )})

(ds/defentity User [^:key user-id])

(defn token []
  (json-str {:token (channel/create-channel (. (current-user) getUserId))}))

(defn send-message-to-users [message users]
  (when users
    (let [name (. (current-user) toString) user (first users)]
      (try
        (channel/send
          (:user-id user)
          (json-str {:user name :message message}))
        (catch ChannelFailureException _
          (ds/delete! user)))
      (send-message-to-users message (next users)))))

(defn opened []
  (let [user (User. (. (current-user) getUserId))]
    (do
      (ds/save! user)
      (send-message-to-users "connected." (ds/query :kind User))))
  (json-str {:status "OK"}))

(defn send-message [{message :message}]
  (send-message-to-users message (ds/query :kind User))
  (json-str {:status "OK" :message message}))

(defroutes private-handler
  (GET "/" [] (index))
  (GET "/token" [] (token))
  (GET "/opened" [] (opened))
  (POST "/send-message" {params :params} (send-message params)))

(defroutes public-handler
  (ANY "*" _ (redirect (login-url))))

(defn main-handler [request]
  (if (user-logged-in?)
    (private-handler request)
    (public-handler request)))

(def app
 (-> #'main-handler
   (wrap-keyword-params)
   (wrap-nested-params)
   (wrap-params)))

(ae/def-appengine-app peechiku-app #'app)
