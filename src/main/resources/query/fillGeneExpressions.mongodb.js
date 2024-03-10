use("singlecell");

cellId = "exp11"
experimentId = "exp1"
resolutionId = "exp1cluster_0.10"
clusterId = "exp1cluster_0.101"
numberOfMarkers = 5
geneCodes = ["Vcpip1", "Tram1", "Gata1", "Naaa"];
db.getCollection("partialGeneExpressionList").aggregate([
    {
        $group: {
          _id: "$geneExpressionListId",
          expressions: {
            $push: "$expressions"
          }
        }
    },
    {
      $project: {
        _id:1,
        expressions: {
          $reduce: {
            input: "$expressions",
            initialValue: [],
            in: { $concatArrays: [ "$$value", "$$this"]}
          }
        }
      }
    },
    {
      $out: 'geneExpressionList2'
    }
  ], {allowDiskUse:true})/*
    {
        $lookup: {
          from: "cell",
          localField: "_id",
          foreignField: "_id",
          as: "cellInfo"
        }
    },
    {
        $lookup: {
          from: "cluster",
          localField: "cellInfo.clusterIds",
          foreignField: "_id",
          as: "clusterInfo"
        }
    },
    {
        $unwind: "$clusterInfo"
    },
    {
        $match: {
            "clusterInfo.resolution": resolutionId
        }
    },
    {
        $project: {
          clusterId: "$clusterInfo._id",
          clusterName: "$clusterInfo.name",
          sampleId: "$cellInfo.sample",
          expressions:1
        }
    },
    {
        $group: {
          _id: {
            clusterId: "$clusterId",
            sampleId: "$sampleId"
        },
          clusterName: {
            $first: "$clusterName"
          },
          expressions: {
            $push: "$expressions"
          }
        }
    },
    
    {
        $lookup: {
          from: "cell",
          localField: "_id.clusterId",
          foreignField: "clusterIds",
          pipeline: [
            {
                $group: {
                    _id: null,
                    cellCount: {
                        $count: {}
                    }
                }
            }
          ],
          as: "cellCount"
        }
    },
    {
        $project: {
            cellCount: "$cellCount.cellCount",
            clusterId: "$_id.clusterId",
            sampleId: "$_id.sampleId",
            expressions:1,
            _id:0

        }
    },
    {
        $unwind: "$sampleId"
    }
    // add sub aggregation with count of cells by cluster and sample
    // if it is not possible perform a separate query
    
    
])/*
  
/* */