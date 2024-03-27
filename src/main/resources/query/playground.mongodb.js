use("singlecell");

experimentId = "exp1"
resolutionId = ObjectId("65fc35eb87c9af7b37edbfcf")
clusterId = ObjectId("65fc35eb87c9af7b37edbfd9")
cellIds = [ObjectId("65fc35eb87c9af7b37edc001"), ObjectId("65fc35eb87c9af7b37edc002"), ObjectId("65fc35eb87c9af7b37edc003")]
numberOfMarkers = 5
geneCodes = ["Vcpip1", "Tram1", "Gata1", "Naaa", "Casp1"]
result = db.getCollection("cell").aggregate([
    {
      $match: {
        "experiment.$id": experimentId,
      }
    },
    { $unwind: "$cellClusters" },
    {
      $group: {
        _id: "$cellClusters.cluster",
        resolution: { $first: "$cellClusters.resolution" },
        cellCount: { $sum: 1 }
      }
    },
    {
      $lookup: {
        from: "cluster",
        localField: "_id.$id",
        foreignField: "_id",
        as: "clusterInfo"
        }
    },
    { $unwind: "$clusterInfo" },
    {
      $group: {
        _id: {
          resolution: "$resolution",
          cluster: "$clusterInfo.name",
        },
        cellCount: { $sum: "$cellCount" }
      }
    },
    {
      $lookup: {
        from: "resolution",
        localField: "_id.resolution.$id",
        foreignField: "_id",
        as: "resolutionInfo"
        }
    },
    { $unwind: "$resolutionInfo" },
    {
      $project: {
        resolution: "$resolutionInfo.name",
        cluster: "$_id.cluster",
        cellCount:1,
        _id:0

      }
    }
    
  ])/*
/* */
print(result);
