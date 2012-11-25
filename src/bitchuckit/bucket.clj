(ns bitchuckit.bucket
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:gen-class))

(def ^:private bucket-filename "/Users/abaranosky/Desktop/BitChuckit.txt")

(defn- load-bucket-from-storage []
  (str/split-lines (slurp bucket-filename)))

(defn- grab-3 [items]
  (take 3 (shuffle items)))

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
  (println)
  (println "Your Sampling:")

  (let [bucket-lines (load-bucket-from-storage)
        lines (grab-3 bucket-lines)
        idx->line {1 (first lines)
                   2 (second lines)
                   3 (nth lines 2)}]
    (doseq-indexed idx [line lines]
                   (println (str "    " (inc idx) ". ") line))

    (println)
    (println "Enter the number of any you want to delete then press ENTER.")
    (let [input (read-line)]
      (when-not (str/blank? input)
        (let [line-indices-to-delete (read-string (str "[" input "]"))
              line-set (set (keep idx->line line-indices-to-delete))
              temp-filename (str "/tmp/" (gensym "bitchuckit-"))]
          (filter-line-seq (complement line-set) bucket-filename temp-filename)
          (spit bucket-filename (slurp temp-filename))
          (io/delete-file temp-filename))))
    (println "Later dude!")))

