use("singlecell");

cellId = "exp11"
experimentId = "exp1"
resolutionId = "exp1cluster_0.10"
clusterId = "exp1cluster_0.101"
numberOfMarkers = 5
db.getCollection("resolution").aggregate([
    {
        $match:{
            _id: resolutionId
        }
    },
    {
        $lookup: {
          from: "cluster",
          localField: "clusters",
          foreignField: "_id",
          as: "clusterInfo"
        }
    },
    {
        $unwind: "$clusterInfo"
    },
    {
        $project: {
            cluster:1,
            topMarkers: {
                $slice: ["$orderedMarkers", 0, numberOfMarkers]
            }
        }
    }
    
])/*
    {
        $project: {
            name:1, 
            markers:1,
            buckets: "$heatmapInfo.buckets",
            expressions: "$heatmapInfo.expressions"
        }
    },
    {
        $unwind: "$buckets"
    },
    {
        $unwind: "$expressions"
    },
 ]);/*
    {
        $match: {
          "cellInfo.clusterIds": clusterId
        }
    },
  
    {
        $project: {
          barcode:1,
          expressions: {
            $map: {
                input: "$markerExpressionsInfo.markerExpressions",
                in: {
                    "barcode":"$barcode",
                    "geneCode":"$$this.geneCode",
                    "expression":"$$this.expression"
                }
            }
          }
          
        }
    },
    
    /* 
    
    get cells
    lookup markerExpressionList
    get only lists from cluster of the selected resolution
    lookup cluster
    project cell barcode or id, expression list, cluster markers and cluster name
    group  by cluster pushing properties into 'cellNames' and 'expressions'

    /*
    get cell
    lookup cluster
    project array with marker names
    lookup expression list
    filter expression list for gene codes in 'marker names'
    */
