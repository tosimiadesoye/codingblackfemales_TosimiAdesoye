package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    @Override
    public Action evaluate(SimpleAlgoState state) {

        final String orderBookAsString = Util.orderBookToString(state);

        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        /********
         *
         * Add your logic here....
         *
         */

        final BidLevel bestBid = state.getBidAt(0);

        final var activeOrders = state.getActiveChildOrders();

        long bestBidPrice = bestBid.price;

        var activeChildOrderCount = state.getChildOrders().size();

        final var option = activeOrders.stream().findFirst();
        long quantity = 200;

        // create one order when there are no active orders in the market
        if (activeChildOrderCount < 1) {
            logger.info("[MyAlgoLogic] Adding first order for: " + quantity + " @ " +
                    bestBidPrice);
            return new CreateChildOrder(Side.BUY, quantity, bestBidPrice);
            // we want to create 6 orders in total. 5 orders at a cheaper price
        } else if (activeChildOrderCount < 6) {
            if (option.isPresent()) {

                var activeChildOrder = option.get();
                // if the next best price in the market is less than our first created order
                if (bestBidPrice < activeChildOrder.getPrice()) {
                    // buy more quantity at a cheaper price than the first order created
                    quantity = 250;

                    logger.info("[MyAlgoLogic] Adding order for: " + quantity + " @ " +
                            bestBidPrice);
                    return new CreateChildOrder(Side.BUY, quantity, bestBidPrice);
                    // but if the next best order is still the same price or higher than our first
                    // order then cancel our initial order
                } else {
                    logger.info("[MyAlgoLogic] Cancelling order: " + activeChildOrder);
                    return new CancelChildOrder(activeChildOrder);
                }
            }
            // once we have 6 orders in the market stop trading
        } else {
            logger.info("[MyAlgoLogic] Have:" + state.getChildOrders().size() + " children, want 6, done.");
            return NoAction.NoAction;
        }

        return NoAction.NoAction;
    }
}
