use("singlecell");

experimentId = "exp1"
resolutionId = ObjectId("65fa0469c8d8d93040294b45")
clusterId = ObjectId("65fc35eb87c9af7b37edbfd9")
cellIds = [ObjectId("65fc35eb87c9af7b37edc001"), ObjectId("65fc35eb87c9af7b37edc002"), ObjectId("65fc35eb87c9af7b37edc003")]
numberOfMarkers = 5
geneCodes = ["0610009B22Rik", "Vcpip1", "Tram1", "Gata1", "Naaa", "Casp1"]
result = db.getCollection("violinGroup").aggregate([
    {
      $match: {
        "resolutionId": resolutionId,
        "code": {
          $in: geneCodes
        },
      }
    },
    {
      $lookup: {
        from: "sample",
        localField: "sampleId",
        foreignField: "_id",
        as: "sampleInfo"
      }
    },
    { $unwind: "$sampleInfo" },
    {
      $lookup: {
        from: "cluster",
        localField: "clusterId",
        foreignField: "_id",
        as: "clusterInfo"
      }
    },
    { $unwind: "$clusterInfo" },
    {
      $project: {
        code:1,
        expressions:1,
        cluster: "$clusterInfo.name",
        sample: "$sampleInfo.name",
      }
    },
    //{ $out: "violinGroups" },


    
  ], {allowDiskUse: true})/*
/* */
print(result);
