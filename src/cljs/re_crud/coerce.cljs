(ns re-crud.coerce
  (:require [re-crud.util :as util]
            [re-frame.core :refer [dispatch]]))

(defn empty-value [t]
  (case t
    "string" ""
    "integer" nil
    "array" []
    "boolean" false))

(declare request)

(defn coerce-param [param schema-type path]
  (cond (and (map? param)
             (sequential? schema-type))
        (mapv #(request % (first schema-type)) (vals param))

        (and (sequential? param)
             (sequential? schema-type))
        (mapv #(request % (first schema-type)) param)

        (= "integer" schema-type)
        (js/parseInt param)

        (= "number" schema-type)
        (js/parseFloat param)

        (= "boolean" schema-type)
        (case (str param)
          "true" true
          "false" false
          (dispatch [:crud-notify :parse-boolean :fail (str "Please select 'true' or 'false' for " path)]))

        :else
        param))

(defn ->map [path-values]
  (reduce (fn [acc [path v]]
            (assoc-in acc path v))
          {}
          path-values))

(defn request [param schema]
  (if (map? schema)
    (->> (for [path (util/paths schema)
               :let [param-value (get-in param path)
                     param-schema (get-in schema path)]
               :when (not (contains? #{nil ""} param-value))]
           [path (coerce-param param-value param-schema path)])
         (into {})
         ->map)
    (coerce-param param schema)))
