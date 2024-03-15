use("singlecell");

cellId = "exp11"
experimentId = "exp1"
resolutionId = "exp1cluster_0.10"
clusterId = ObjectId("65f3638fe17fe578f8791735")
numberOfMarkers = 5
geneCodes = ["Vcpip1", "Tram1", "Gata1", "Naaa"];
db.getCollection("cellExpressionList").aggregate([
    {
      $lookup: {
        from: "cell",
        localField: "cell.$id",
        foreignField: "_id",
        as: "cellInfo"
      }

    },
    {
      $match: {
        "cellInfo.cellClusters": {
          $elemMatch: { "cluster.$id": clusterId
          }
        }
      }
    },
    {
      $project: {
        barcode: "$cellInfo.barcode",
        expressions: {
          $filter: {
            input: "$expressions",
            as: "expression",
            cond: {
              $in: ["$$expression.code", geneCodes]
            }
          }
        }
      }
    }
    
  ])/*
    {
      $out: 'cellExpressionList2'
    }
  ], {allowDiskUse:true})/*
/*
db.cluster.aggregate([ 
  {
    $lookup: { 
      from:"resolution",
      localField:"resolution.$id",
      foreignField:"_id",
      as:"resolutionInfo"
    }
  },
  {
    $match:{
      "resolutionInfo.experiment.$id":"exp1"
    }
  }
])
/* */