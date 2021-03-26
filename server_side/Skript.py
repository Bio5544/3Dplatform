
##Import the Modules and libraries
import sys
import json
import os
import requests
import time
from qgis.core import *
from qgis.analysis import QgsNativeAlgorithms
from PyQt5.QtCore import *
import config


## extract the variables from Config.py
Path_ServerFolder=config.Path_ServerFolder
Bbox_Boundry=config.Bbox_Boundry
color=config.color
qgis_path=config.qgis_path
qgisPlugins_path=config.qgisPlugins_path
camera_LatLon=config.camera_LatLon




## initialize the Qgis application .
QgsApplication.setPrefixPath(r"{}".format(qgis_path),True)
app=QgsApplication([],True)
QgsApplication.initQgis()
sys.path.append(r"{}".format(qgisPlugins_path))


## initialize the processing and add the nativeAlgorithmus to it .
import processing
from processing.core.Processing import Processing
Processing.initialize()
QgsApplication.processingRegistry().addProvider(QgsNativeAlgorithms())

server_Folder_Pfad=r"{}".format(Path_ServerFolder)
current_path=os.getcwd()+"\\temp"

try:
    filelist = [ f for f in os.listdir(current_path) ]
    for c in filelist:
        os.remove(os.path.join(current_path, c))
except:
    pass

## remove alle geojson files on the Server
try:
    filelist = [ f for f in os.listdir(server_Folder_Pfad) if f.endswith(".geojson") ]
    for f in filelist:
        os.remove(os.path.join(server_Folder_Pfad, f))
except:
    pass

##remove the levels_num.json file on the server .
try:
    filelist2 = [ f for f in os.listdir(server_Folder_Pfad) if f=="levels_num.json" ]
    for k in filelist2:
        os.remove(os.path.join(server_Folder_Pfad, k))
except:
    pass

##Query to Osm and get the json data:
    ## the Boundry taken from the config file .
overpass_url = "http://overpass-api.de/api/interpreter"
overpass_query = """
[out:json];
(
 way[level]
  ({});
);
out geom;
""".format(Bbox_Boundry)

## get the response as json file
response = requests.get(overpass_url,
                        params={'data': overpass_query})
data = response.json()

## the json file will save in the Temp file .
with open(current_path+'\\json.txt', 'w+') as json_file:
    json.dump(data, json_file)

data2 = json.load(open(current_path+"\\json.txt"))


## this function to define the color scheme for this project.
##The walls will take the color from the configuration file and the floor will be gray,
##the Tolite will be yellow and the stairs will be red.
def check(i,b):
    if b==2 or b==1:
        a=""
        if  ";" in i['tags'].get("level","") or ";" not in i['tags'].get("level","") :
            if ((i['tags'].get("room") in ["stairs","elevator",""])):
                a="red"

            if i['tags'].get("door") and (not i['tags'].get("room") or not i['tags'].get("indoor")):
                a=config.color


            if (i['tags'].get("stairwell")=="elevator" and not\
             i['tags'].get("highway") ) and\
              (not i['tags'].get("room") or not \
              i['tags'].get("indoor")):

                a="red"

            if i["tags"].get("stairs"):
                a="red"

            if i['tags'].get("room","")=="toilets":
                a="yellow"
            if not a :
                a=config.color
            return a


n=0

list_level=""

##here is a loop for 50 floors in each round the json file is read and the desired Eatge is searched for,
## if the script finds the desired floor then a Geojson file with a certain structure is created for this floor.
##then the geojson file is read to create a shape file.

for i in range(0,50):
    geojson5={'features':[],
         "type": "FeatureCollection"}

    for d in data2['elements']:
        if '{}'.format(i) == d['tags'].get("level",""):
            if ((int(d['tags'].get("level",""))>=0) and \
            ((len(d['geometry'])>2 or d['tags'].get("heighway")=="steps"))) and \
            (d['tags'].get("indoor","") or\
             d['tags'].get("stairwell","") or\
             d['tags'].get("stairs","") or\
             d['tags'].get("room","")=="stairs") :


                geojson5['features'].append({
                        "type": "Feature",
                        "properties" :{
                                        "id":n+1,
                                        "name":d["tags"].get("ref",""),
                                        "height":3.1*(i+1),
                                        "base_height":3.2*i,
                                        "level":int('{}'.format(i)),
                                        "door":d["tags"].get("door","noInfo"),
                                        "indoor":d["tags"].get("indoor",""),
                                        "room":d["tags"].get("room",""),
                                        "color":check(d,1)},
                        "geometry" : {
                            "type": "MultiPolygon",
                            "coordinates": [[[[t["lon"],t["lat"]] for t in d['geometry']]]],
                            },
                     })
                n+=1

            else:
                pass

        if "{};".format(i) in d['tags'].get("level","") :

            if (len(d['geometry'])>2 or d['tags'].get("heighway")=="steps") and\
            (d['tags'].get("indoor","") or\
             d['tags'].get("stairwell","") or \
             d['tags'].get("stairs","") or \
             d['tags'].get("room","")=="stairs"):

                geojson5['features'].append({
                    "type": "Feature",
                    "properties" :{
                                    "id":n+1,
                                    "name":d["tags"].get("ref",""),
                                    "height":3.1*(i+1),
                                    "base_height":3.1*i,
                                    "level":int('{}'.format(i)),
                                    "door":d["tags"].get("door","noInfo"),
                                    "indoor":d["tags"].get("indoor",""),
                                    "room":d["tags"].get("room",""),
                                    "color":check(d,2)},
                    "geometry" : {
                        "type": "MultiPolygon",
                        "coordinates": [[[[t["lon"],t["lat"]] for t in d['geometry']]]],
                        },
                 })
                n+=1
            else:
                pass

            ## create a shapefile and make the Field map .
            ## Read the geojson file for this Floor and put the Information in the Attributes for the shapefile .
    if len(geojson5["features"])!=0:

        list_level+=str(i)+";"
        ## Field map : id:int ,name:string ,hieght:real ,base_height:real ,level:int ,door:string ,indoor: string, room:string ,color:string.
        layerFields = QgsFields()
        layerFields.append(QgsField('id',QVariant.Int))
        layerFields.append(QgsField('name',QVariant.String))
        layerFields.append(QgsField('hieght',QVariant.Double, 'double', 20, 1))
        layerFields.append(QgsField('base_heigh',QVariant.Double, 'double', 20, 1))
        layerFields.append(QgsField('level',QVariant.Int))
        layerFields.append(QgsField('door',QVariant.String))
        layerFields.append(QgsField('indoor',QVariant.String))
        layerFields.append(QgsField('room',QVariant.String))
        layerFields.append(QgsField('color',QVariant.String))

        fn=current_path+"\\level_Orginal{}.shp".format(i)
        writer = QgsVectorFileWriter(fn, 'UTF-8', layerFields, QgsWkbTypes.Polygon, QgsCoordinateReferenceSystem('EPSG:4326'), 'ESRI Shapefile')

        for feature in geojson5['features']:
            elem= QgsFeature()
            geom = QgsGeometry.fromPolygonXY([[QgsPointXY(pt[0],pt[1])  for pt in feature['geometry']['coordinates'][0][0]]])
            elem.setGeometry(geom)##feature['geometry']
            elem.setAttributes([feature['properties']['id'],
                                feature['properties']['name'],
                                round(float(feature['properties']['height']),1),
                                round(float(feature['properties']['base_height']),1),
                                feature['properties']['level'],
                                feature['properties']['door'],
                                feature['properties']['indoor'],
                                feature['properties']['room'],
                                feature['properties']['color']])
            writer.addFeatures( [ elem ] )

        del(writer)


        ## fix the Geometry in the shapefile .
        fn_fix=processing.run("native:fixgeometries", {'INPUT': fn,
                      'OUTPUT': "memory:tempFix3"})
        fnFix=fn_fix['OUTPUT']


        ## make a buffer for the previous result.
        res1=processing.run("native:buffer", {'INPUT': fnFix,
                      'DISTANCE': -0.000003,
                      'SEGMENTS': 5,
                      'DISSOLVE': False,
                      'END_CAP_STYLE': 1,
                      'JOIN_STYLE': 1,
                      'MITER_LIMIT': 2.000000,
                      'OUTPUT': "memory:temp1"})
        inputBuffer1 = res1['OUTPUT']



        ## fix the Geometry again for the previous result
        res1_fix=processing.run("native:fixgeometries", {'INPUT': inputBuffer1,
                      'OUTPUT': "memory:tempFix"})



        ## calculate the Center of each Polygon in the previous result.
        inputBuffer = res1_fix['OUTPUT']
        res1_label=processing.run("native:centroids",
                                    {'INPUT': inputBuffer,
                                     'ALL_PARTS':False,
                                     'OUTPUT': "memory:temp2"})


        ## create the Geojson file for the Label . the Geometry for this file is Points. the result name {}POI.geojson for each level .
        resultat_label=res1_label['OUTPUT']
        features_label=resultat_label.getFeatures()
        geojson5_label={'features':[],
                 "type": "FeatureCollection"}

        for feat2 in features_label:
            if feat2.geometry():
                geo= QgsGeometry.asPoint(feat2.geometry())
                pxy=QgsPointXY(geo)
                geojson5_label['features'].append({
                                    "type": "Feature",
                                    "properties" :{
                                                    "id":feat2.attributes()[0],
                                                    "name":feat2.attributes()[1] if feat2.attributes()[1] else "" ,
                                                    "height":feat2.attributes()[2],
                                                    "base_height":feat2.attributes()[3],
                                                    "level":feat2.attributes()[4],
                                                    "door":feat2.attributes()[5] if feat2.attributes()[5] else "" ,
                                                    "indoor":feat2.attributes()[6] if feat2.attributes()[6] else "" ,
                                                    "room":feat2.attributes()[7] if feat2.attributes()[7] else "" ,
                                                    "color":feat2.attributes()[8]},
                                    "geometry" : {
                                    "type": "Point",
                                    "coordinates": [pxy.x(),pxy.y()],},
                                 })
        print(len(geojson5_label['features']))
        print("#################################")


        l=i
        if len(geojson5_label["features"])!=0:
            output1 = open(server_Folder_Pfad+"\{}POI.geojson".format(l), 'w+', encoding='utf-8')
            json.dump(geojson5_label, output1)
            output1.close()


        ## the result from buffer is the ground , because of that must change the base height of the Feature .
        layer_provider=inputBuffer.dataProvider()
        features_buffer=inputBuffer.getFeatures()
        inputBuffer.startEditing()

        lenth=float(geojson5['features'][0]['properties']['base_height'])+0.1

        for f in features_buffer:
            id=f.id()
            attr_value={2:lenth}
            attr_value2={8:"gray"}
            layer_provider.changeAttributeValues({id:attr_value})
            inputBuffer.updateFields()
            layer_provider.changeAttributeValues({id:attr_value2})
            inputBuffer.updateFields()

        inputBuffer.commitChanges()


        ## Calculate the difference between the buffer-Result and the input-data (fnFix) to extract the walls .
        print("differnce.....")



        res2=processing.run("native:difference",
                       {'INPUT': fnFix,
                        'OVERLAY':inputBuffer,
                        'OUTPUT': "memory:temp3"})


        inputDifference=res2['OUTPUT']



        ## merge the buffer result with the previous result of difference to get the full shape file .
        print("mergeVector...........")
        res3=processing.run("native:mergevectorlayers",
                            {'LAYERS':[inputBuffer,inputDifference],
                             'CRS':'EPSG:4326',
                             'OUTPUT': "memory:temp4"})


        output_merge= res3['OUTPUT']


        ## Read the Full shapefile and write the full Geijson file for each floor .
        ###geojson zu erstellen #################################
        geojson5_2={'features':[],
                 "type": "FeatureCollection"}


        features_resultat=output_merge.getFeatures()

        #feat.attributes()
        for feat in features_resultat:
            if feat.geometry():
                geom=QgsGeometry.asMultiPolygon(feat.geometry())[0]


                geojson5_2['features'].append({
                                    "type": "Feature",
                                    "properties" :{
                                                    "id":feat.attributes()[0],
                                                    "name":feat.attributes()[1] if feat.attributes()[1] else "",
                                                    "height":feat.attributes()[2],
                                                    "base_height":feat.attributes()[3],
                                                    "level":feat.attributes()[4],
                                                    "door":feat.attributes()[5] if feat.attributes()[5] else "" ,
                                                    "indoor":feat.attributes()[6] if feat.attributes()[6] else "" ,
                                                    "room":feat.attributes()[7] if feat.attributes()[7] else "" ,
                                                    "color":feat.attributes()[8]},
                                    "geometry" : {
                                    "type": "MultiPolygon",
                                    "coordinates": [[[[QgsPointXY(k).x(),QgsPointXY(k).y()] for k in t] for t in geom]],
                                    },
                                 })
        print(len(geojson5_2['features']))
        print("###########################################################")
        kk=i
        if len(geojson5["features"])!=0:
            output = open(server_Folder_Pfad+"\{}.geojson".format(kk), 'w+',encoding='utf-8')
            json.dump(geojson5_2, output)
            output.close()


list_level+=config.color+";"+config.name+";"+camera_LatLon

## write the levels_num.json file , which contain (the levels , the Color , name , the cordinates of the Center of the Camera )
file1 = open(server_Folder_Pfad+"\levels_num.json","w+")
file1.write('[{"level":'+'"{}"'.format(list_level)+'}]')
file1.close()

## close the Qgis Application .
QgsApplication.exitQgis()
print("Fertig...............")

