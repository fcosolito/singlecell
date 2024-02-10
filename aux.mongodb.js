use("singlecell");

resolutionId = "exp1cluster_0.20"
experimentId = "exp1"

db.getCollection("experiment").aggregate([
    {
      $match: {
        _id: experimentId
      }
    },
    {
      $unwind: "$cells"
    },
    {
      $lookup: {
        from: "cell",
        localField: "cells",
        foreignField: "_id",
        as: "cellInfo"
      }
    },
    {
      $unwind: "$cellInfo"
    }, 
    {
      $lookup: {
        from: "cluster",
        localField: "cellInfo.clusters",
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
      $lookup: {
        from: "resolution",
        let: { resolution: "$clusterInfo.resolution"},
        pipeline: [
          {
            $match: {
              $expr: { $eq: ["$_id", "$$resolution"]}
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
              markers: "$clusterInfo.markers.geneCode"
            }
          },
          {
            $group: {
              _id: "$_id",
              markers: {$push: "$markers"}
            }
          },
          {
            $project: {
              markers: {
                $reduce: {
                  input: "$markers",
                  initialValue: [],
                  in: { $concatArrays: [ "$$value", "$$this" ] }
                }
              },
              
            }
          }
        ],
        as: "markersFullList"
      }
    },
    {
      $project: {
        experimentId: "$_id",
        name: "$clusterInfo.name",
        clusterMarkers: "$clusterInfo.markers.geneCode",
        markersList: "$markersFullList.markers",
        expressions: "$cellInfo.geneExpressions",
        barcode: "$cellInfo.barcode"
      }
    },
    {
      $unwind: "$markersList"
    },
    {
      $lookup: {
        from: "geneExpression",
        let: {
          expressions: "$expressions",
          markersList: "$markersList",
        },
        pipeline: [
          {
            $match: {
              $expr: { $and: [
                {$in: ["$geneCode", "$$markersList"]},
                {$in: ["$_id", "$$expressions"]}
                ]
              }
            }
          },
        ],
        as: "expressions"

      }
    },
  ])/*
    {
      $unwind: "$expressionInfo"
    },
    {
      $project: {
        name:1,
        clusterMarkers:1,
        barcode:1,
        filteredExpressions: {
          $filter: { // Hacer eficiente esto, capaz con una agregacion anidada
            // sacar a la mierda la lista de expresiones y indexar por gen las expresiones
            input: "$expressionInfo.geneExpressions",
            as: "expression",
            cond: { $in: ["$$expression.geneCode", "$markersList"]}
          }
        }
      }
    },
    {
      $group: {
        _id: "$name",
        clusterMarkers: {
          $first: "$clusterMarkers"
        },
        barcodes: {$push: "$barcode"},
        expressions: {$push: "$filteredExpressions"}
      }
    },
  ])/*
    {
      $project: {
        _id:1,
        clusterMarkers:1,
        barcodes:1,
        expressions: {
          $reduce: {
            input: "$expressions",
            initialValue: [],
            "in": { $concatArrays: [ "$$value", "$$this" ] }
          }
        }
      }
    }
 
 
  ])
    /*
    {
      $match: {
        "resolutionInfo._id": resolutionId
      }
    },
    {
      $lookup: {
        from: "cluster",
        localField: "resolutionInfo.clusters",
        foreignField: "_id",
        as: "clusterInfo"
      }
    },
    {
      $unwind: "$cells"
    },
    {
      $lookup: {
        from: "cell",
        localField: "cells",
        foreignField: "_id",
        as: "cellInfo"
      }
    },
    {
      $unwind: "$cellInfo"
    },
    {
      $unwind: "$clusterInfo"
    },
    {
      project: {
        name: "$clusterInfo.name",
        markers: "$clusterInfo.markers",


      }
    },

  ]) 
    {
      $project: {
        barcode: "$cellInfo.barcode",
        clusterInfo: {
          $filter: {
            input: "$clusterInfo",
            as: "cluster",
            cond: {$eq: ["$$cluster.resolution", "exp1cluster_0.20"]}
          }
        }
      }

    },
    
    {
      $unwind: "$clusterInfo"
    },
    {
      $group: {
        _id: "$_id",
        barcodes: {$push: "$barcode"},
        clusterNames: {$push: "$clusterInfo.name"},

      }
    }

  ])
  */