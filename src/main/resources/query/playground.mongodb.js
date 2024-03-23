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
      $lookup: {
        from: "cluster",
        localField: "cellClusters.cluster.$id",
        foreignField: "_id",
        as: "clusterInfo"
        }
    },
    { $unwind: "$clusterInfo" },
    {
      $project: {
      barcode:1,
      spring1:1,
      spring2:1,
      pca1:1,
      pca2:1,
      tsne1:1,
      tsne2:1,
      umap1:1,
      umap2:1,
      cluster: "$clusterInfo.name",
      }
    },
    {
      $group: {
        _id:null,
        barcodes: { $push: "$barcode" },
        spring1: { $push: "$spring1" },
        spring2: { $push: "$spring2" },
        umap1: { $push: "$umap1" },
        umap2: { $push: "$umap2" },
        tsne1: { $push: "$tsne1" },
        tsne2: { $push: "$tsne2" },
        pca1: { $push: "$pca1" },
        pca2: { $push: "$pca2" },
        clusters: { $push: "$cluster" },
        cellCount: { $sum: 1 }
    }
  }
    
  ])/*
/* */
print(result);
