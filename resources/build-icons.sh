#!/bin/bash

RES="$HOME/Android/UNCmorfi/app/src/main/res"
DIR="mipmap"
ICON=$1
NAME="ic_launcher.png"

declare -A SIZES=(["mdpi"]="48"
                  ["hdpi"]="72"
                  ["xhdpi"]="96"
                  ["xxhdpi"]="144"
                  ["xxxhdpi"]="192")

echo "Build icons with diferent size"
echo "Export to $RES/$DIR*"
echo "Using $1"


for size in "${!SIZES[@]}";
do
    echo "Building $size..."
    E=$RES/$DIR-$size/$NAME
    WH=${SIZES[$size]}
    inkscape -z $ICON -e $E -w $WH -h $WH
done;
