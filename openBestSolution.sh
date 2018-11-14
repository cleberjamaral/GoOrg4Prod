#!/bin/bash

rm *.gv.pdf
rm *.gv

java -jar orgDesign.jar $1 3
dot -Tpdf graph_1.gv -o graph_1.gv.pdf
evince graph_1.gv.pdf



#a=1
#for i in *.gv; do
#  new=$(printf "graph_%d.pdf" "$a")
#  dot -Tpdf $i -o "$new"
#  let a=a+1
#done

