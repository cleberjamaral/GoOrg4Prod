#!/bin/bash

rm *.gv.pdf

for i in *.gv; do
  new=$(printf "%s.pdf" "$i")
  dot -Tpdf $i -o "$new"
done


#a=1
#for i in *.gv; do
#  new=$(printf "graph_%d.pdf" "$a")
#  dot -Tpdf $i -o "$new"
#  let a=a+1
#done

rm *.gv
