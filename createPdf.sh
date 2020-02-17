#!/bin/bash

rm *.pdf

mkdir -p output/graphs
cd output/diagrams


#for i in *.gv; do
#  number=$(echo $i | tr -cd '[[:digit:]]')
#  new=$(printf "%s.pdf" "$i")
#  dot -Tpdf $i -o "../graphs/orgTree_graph_$number.pdf"
#done
#rm *.gv


a=1
for i in *.gv; do
  new=$(printf "graph_%d.pdf" "$a")
  dot -Tpdf $i -o "../graphs/$new"
  let a=a+1
done

