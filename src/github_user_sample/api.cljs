(ns github-user-sample.api
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))


;; HTTP Request helper
(defn make-remote-call [endpoint cb]
  (go
    (let [response (<! (http/get endpoint {:with-credentials? false}))]
      (cb response))))


