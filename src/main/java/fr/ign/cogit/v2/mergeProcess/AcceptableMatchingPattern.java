package fr.ign.cogit.v2.mergeProcess;

import fr.ign.cogit.v2.lineage.MatchingLink;


public enum AcceptableMatchingPattern{
    SINGLENODE_SINGLENODE(Multiplicity.SINGLE,Multiplicity.SINGLE,Multiplicity.NONE,Multiplicity.NONE),
    SINGLENODE_GROUP(Multiplicity.SINGLE, Multiplicity.MULTIPLE_2, Multiplicity.NONE, Multiplicity.MULTIPLE_1),
    GROUP_SINGLENODE(Multiplicity.MULTIPLE_2,Multiplicity.SINGLE, Multiplicity.MULTIPLE_1, Multiplicity.NONE),
    GROUP_GROUP(Multiplicity.MULTIPLE_2,Multiplicity.MULTIPLE_2, Multiplicity.MULTIPLE_1, Multiplicity.MULTIPLE_1),
    SINGLEEDGE_SINGLEEDGE(Multiplicity.NONE, Multiplicity.NONE, Multiplicity.SINGLE,Multiplicity.SINGLE),
    SINGLEEDGE_MULTIPLEEDGE(Multiplicity.NONE, Multiplicity.NONE, Multiplicity.SINGLE,Multiplicity.MULTIPLE_2),
    MULTIPLEEDGE_SINGLEEDGE(Multiplicity.NONE, Multiplicity.NONE, Multiplicity.MULTIPLE_2,Multiplicity.SINGLE),
    MULTIPLEEDGE_MULTIPLEEDGE(Multiplicity.NONE, Multiplicity.NONE, Multiplicity.MULTIPLE_2,Multiplicity.MULTIPLE_2);


    /**
     * Est-ce que un lien d'appariemejt générique match un pattern donné
     * @param link
     * @param pattern
     * @return
     */
    public boolean isMatchedBy(MatchingLink link){
        if(link.getSources().getNodes().size() >= this.getMinSourceNodesSize()
                && link.getSources().getNodes().size() <= this.getMaxSourceNodesSize()
                && link.getTargets().getNodes().size() >= this.getMinTargetNodesSize()
                && link.getTargets().getNodes().size() <= this.getMaxTargetNodesSize()
                && link.getSources().getEdges().size() >= this.getMinSourceEdgesSize()
                && link.getSources().getEdges().size() <= this.getMaxSourceEdgesSize()
                && link.getTargets().getEdges().size() >= this.getMinTargetEdgesSize()
                && link.getTargets().getEdges().size() <= this.getMaxTargetEdgesSize()){
            return true;
        }
        return false;
    }

    public Multiplicity getMultiplicitySourceNodes() {
        return multiplicitySourceNodes;
    }

    public void setMultiplicitySourceNodes(Multiplicity multiplicitySourceNodes) {
        this.multiplicitySourceNodes = multiplicitySourceNodes;
    }

    public Multiplicity getMultiplicityTargetNodes() {
        return multiplicityTargetNodes;
    }

    public void setMultiplicityTargetNodes(Multiplicity multiplicityTargetNodes) {
        this.multiplicityTargetNodes = multiplicityTargetNodes;
    }

    public Multiplicity getMultiplicitySourceEdges() {
        return multiplicitySourceEdges;
    }

    public void setMultiplicitySourceEdges(Multiplicity multiplicitySourceEdges) {
        this.multiplicitySourceEdges = multiplicitySourceEdges;
    }

    public Multiplicity getMultiplicityTargetEdges() {
        return multiplicityTargetEdges;
    }

    public void setMultiplicityTargetEdges(Multiplicity multiplicityTargetEdges) {
        this.multiplicityTargetEdges = multiplicityTargetEdges;
    }
    
    public int getMinSourceNodesSize(){
        return this.multiplicitySourceNodes.getMinSize();
    }
    public int getMaxSourceNodesSize(){
        return this.multiplicitySourceNodes.getMaxSize();
    }
    
    public int getMinTargetNodesSize(){
        return this.multiplicityTargetNodes.getMinSize();
    }
    public int getMaxTargetNodesSize(){
        return this.multiplicityTargetNodes.getMaxSize();
    }
       
    public int getMinSourceEdgesSize(){
        return this.multiplicitySourceEdges.getMinSize();
    }
    public int getMaxSourceEdgesSize(){
        return this.multiplicitySourceEdges.getMaxSize();
    }
    
    public int getMinTargetEdgesSize(){
        return this.multiplicityTargetEdges.getMinSize();
    }
    public int getMaxTargetEdgesSize(){
        return this.multiplicityTargetEdges.getMaxSize();
    }
    
    
    private Multiplicity multiplicitySourceNodes;
    private Multiplicity multiplicityTargetNodes;
    private Multiplicity multiplicitySourceEdges;
    private Multiplicity multiplicityTargetEdges;

    AcceptableMatchingPattern(Multiplicity multiplicitySourceNodes, Multiplicity multiplicityTargetNodes
            , Multiplicity multiplicitySourceEdges, Multiplicity multiplicityTargetEdges){
        this.multiplicitySourceNodes =multiplicitySourceNodes;
        this.multiplicityTargetNodes =multiplicityTargetNodes;
        this.multiplicitySourceEdges =multiplicitySourceEdges;
        this.multiplicityTargetEdges =multiplicityTargetEdges;
    }

    private enum Multiplicity{
        NONE(0,0),
        SINGLE(1,1),
        DOUBLE(2,2),
        MULTIPLE_1(1,Integer.MAX_VALUE),
        MULTIPLE_2(2,Integer.MAX_VALUE);

        public int getMinSize() {
            return minSize;
        }

        public int getMaxSize() {
            return maxSize;
        }

        private int minSize;
        private int maxSize;
        Multiplicity(int minSize, int maxSize){
            this.minSize = minSize;
            this.maxSize = maxSize;
        }
    }

}


