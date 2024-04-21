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
      $match: {
        "cellClusters.resolution.$id": resolutionId
      }
    },
    {
      $group: {
        _id: {
          sample: "$sample.$id",
          cluster: "$cellClusters.cluster.$id",
        },
        // change this
        resolution: { $first: "$cellClusters.resolution.$id" },
        cells: { $push: "$_id" },
        //cellCount: { $sum: 1 },
      }
    },
    {
      $project: {
        sample: "$_id.sample",
        cluster: "$_id.cluster",
        resolution: 1,
        cells: 1,
      }
    },
    //{ $out: "violinGroups" },


    
  ], {allowDiskUse: true})/*
/* */
print(result);
