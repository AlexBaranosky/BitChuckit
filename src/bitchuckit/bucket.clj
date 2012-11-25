(ns bitchuckit.bucket
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:gen-class))

(def ^:private bucket-filename "/Users/abaranosky/Desktop/BitChuckit.txt")
(def ^:private bitchuckit (atom []))

(defn- load-bucket-from-storage []
  (reset! bitchuckit (vec (str/split-lines (slurp bucket-filename)))))

(defn- grab-3 []
  (take 3 (shuffle @bitchuckit)))

(defn- filter-line-seq
  "Filter one file into another file one line at a time.
   Only needs to keep one line in memory at a time."
  [pred input output]
  (with-open [in (io/reader input)
              out (io/writer output)]
    (binding [*out* out]
      (doseq [line (line-seq in)]
        (when (pred line)
          (println line)))))) 

(defmacro doseq-indexed [index-sym [item-sym coll] & body]
  `(doseq [[~item-sym ~index-sym] (map vector ~coll (range))]
     ~@body))

(defn -main [& args]
  (println "Bitchuckit at your service.")
  (load-bucket-from-storage)

  (println)
  (println "Your Sampling:")
  (let [items (grab-3)
        idx->item {1 (first items)
                   2 (second items)
                   3 (nth items 2)}]
    (doseq-indexed idx [item items]
                   (println (str "    " (inc idx) ". ") item))

    (println)
    (println "Enter the number of any you want to delete then press ENTER.")
    (let [to-delete (read-line)]
      (when-not (str/blank? to-delete)
        (let [to-delete-numbers  (read-string (str "[" to-delete "]"))
              to-delete-set (set (map idx->item to-delete-numbers))
              temp-filename (str "/tmp/" (gensym "bitchuckit-"))]
          (filter-line-seq (complement to-delete-set) bucket-filename temp-filename)
          (spit bucket-filename (slurp temp-filename))
          (io/delete-file temp-filename))))
    (println "Later dude!")))

