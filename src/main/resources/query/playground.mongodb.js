use("singlecell");

experimentId = "exp1"
resolutionId = ObjectId("65fc35eb87c9af7b37edbfcf")
clusterId = ObjectId("65fc35eb87c9af7b37edbfd9")
cellIds = [ObjectId("65fc35eb87c9af7b37edc001"), ObjectId("65fc35eb87c9af7b37edc002"), ObjectId("65fc35eb87c9af7b37edc003")]
numberOfMarkers = 5
geneCodes = ["Vcpip1", "Tram1", "Gata1", "Naaa", "Casp1"]
result = db.getCollection("cluster").aggregate([
    {
      $match: {
        "resolution.$id": resolutionId
      }
    },
    {
      $lookup: {
        from: "heatmapCluster",
        localField: "_id",
        foreignField: "cluster.$id",
        as: "heatmapInfo"
        }
    },
    {
      $unwind: "$heatmapInfo"
    },
    {
      $project: {
      name:1,
      //expr: "$expressionInfo.expressions",
      expressions: "$heatmapInfo.expressions",
      buckets: "$heatmapInfo.buckets",
      markers: "$heatmapInfo.topMarkers",
        
      }
    }
    
  ])/*
/* */
print(result);
