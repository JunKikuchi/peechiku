(ns peechiku.core
  (:require [appengine-magic.core :as ae]))


(defn peechiku-app-handler [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello, world!"})


(ae/def-appengine-app peechiku-app #'peechiku-app-handler)