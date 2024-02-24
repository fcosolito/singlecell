use("singlecell");

resolutionId = "exp1cluster_0.10"
numberOfMarkers = 5
db.getCollection("cluster").aggregate([
    {
        $match:{
            resolution: resolutionId
        }
    },
    
    {
        $project: {
            clusterName: "$name",
            topMarkers: {
                $slice: ["$markers.geneCode", 0, numberOfMarkers]
            }
        }
    },
])/*
    {
        $project: {
          clusterName:1,
          topMarkers: {
            $map: {
                input: "$topMarkers",
                in: "$$this.geneCode"
            }
          }
        }
    }
    
    
  /**/
