/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015  
 */
package vwap;

import java.util.List;

import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.function.BiFunction;
import com.ibm.streamsx.topology.function.Function;

public class VwapProcessing {

    @SuppressWarnings("serial")
    public static TStream<Bargain> bargains(TStream<Trade> trades,
            TStream<Quote> quotes) {

        TStream<VWapT> vwap = trades.last(4).aggregate(
                new Function<List<Trade>, VWapT>() {

                    @Override
                    public VWapT apply(List<Trade> tuples) {
                        VWapT vwap = null;
                        for (Trade trade : tuples) {
                            if (vwap == null)
                                vwap = new VWapT(trade);
                            vwap.newTrade(trade);
                        }
                        return vwap == null ? null : vwap.complete();
                    }
                });

        TStream<Bargain> bargainIndex = quotes.joinLast(vwap,
                new BiFunction<Quote, VWapT, Bargain>() {

                    @Override
                    public Bargain apply(Quote v1, VWapT v2) {
                        return new Bargain(v1, v2);
                    }
                });

        return bargainIndex;
    }
}
