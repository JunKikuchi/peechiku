(ns peechiku.app_servlet
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use peechiku.core)
  (:use [appengine-magic.servlet :only [make-servlet-service-method]]))


(defn -service [this request response]
  ((make-servlet-service-method peechiku-app) this request response))
