/*
 * Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of BetaSteward_at_googlemail.com.
 */
package mage.client;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar.Separator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import mage.cards.decks.Deck;
import mage.cards.repository.CardCriteria;
import mage.cards.repository.CardInfo;
import mage.cards.repository.CardRepository;
import mage.client.cards.BigCard;
import mage.client.chat.ChatPanelBasic;
import mage.client.components.MageComponents;
import mage.client.components.MageDesktopManager;
import mage.client.components.MageJDesktop;
import mage.client.components.MageRoundPane;
import mage.client.components.MageUI;
import mage.client.components.ext.dlg.DialogManager;
import mage.client.components.tray.MageTray;
import mage.client.constants.Constants.DeckEditorMode;
import mage.client.deckeditor.DeckEditorPane;
import mage.client.deckeditor.collection.viewer.CollectionViewerPane;
import mage.client.dialog.AboutDialog;
import mage.client.dialog.ConnectDialog;
import mage.client.dialog.ErrorDialog;
import mage.client.dialog.FeedbackDialog;
import mage.client.dialog.GameEndDialog;
import mage.client.dialog.MageDialog;
import mage.client.dialog.PreferencesDialog;
import mage.client.dialog.TableWaitingDialog;
import mage.client.dialog.UserRequestDialog;
import mage.client.draft.DraftPane;
import mage.client.draft.DraftPanel;
import mage.client.game.GamePane;
import mage.client.game.GamePanel;
import mage.client.plugins.impl.Plugins;
import mage.client.preference.MagePreferences;
import mage.client.remote.CallbackClientImpl;
import mage.client.table.TablesPane;
import mage.client.tournament.TournamentPane;
import mage.client.util.EDTExceptionHandler;
import mage.client.util.GUISizeHelper;
import mage.client.util.SettingsManager;
import mage.client.util.SystemUtil;
import mage.client.util.audio.MusicPlayer;
import mage.client.util.gui.ArrowBuilder;
import mage.client.util.gui.countryBox.CountryUtil;
import mage.client.util.stats.UpdateMemUsageTask;
import mage.components.ImagePanel;
import mage.constants.PlayerAction;
import mage.interfaces.MageClient;
import mage.interfaces.callback.CallbackClient;
import mage.interfaces.callback.ClientCallback;
import mage.remote.Connection;
import mage.remote.Connection.ProxyType;
import mage.remote.Session;
import mage.remote.SessionImpl;
import mage.utils.MageVersion;
import mage.view.GameEndView;
import mage.view.UserRequestMessage;
import net.java.balloontip.BalloonTip;
import net.java.balloontip.positioners.LeftAbovePositioner;
import net.java.balloontip.styles.EdgedBalloonStyle;
import net.java.truevfs.access.TArchiveDetector;
import net.java.truevfs.access.TConfig;
import net.java.truevfs.kernel.spec.FsAccessOption;
import org.apache.log4j.Logger;
import org.mage.card.arcane.ManaSymbols;
import org.mage.plugins.card.images.DownloadPictures;
import org.mage.plugins.card.info.CardInfoPaneImpl;
import org.mage.plugins.card.utils.impl.ImageManagerImpl;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class MageFrame extends javax.swing.JFrame implements MageClient {

    private static final String TITLE_NAME = "XMage";

    private static final Logger LOGGER = Logger.getLogger(MageFrame.class);
    private static final String LITE_MODE_ARG = "-lite";
    private static final String GRAY_MODE_ARG = "-gray";
    private static final String FILL_SCREEN_ARG = "-fullscreen";

    private static MageFrame instance;

    private static Session session;
    private ConnectDialog connectDialog;
    private final ErrorDialog errorDialog;
    private static CallbackClient callbackClient;
    private static final Preferences PREFS = Preferences.userNodeForPackage(MageFrame.class);
    private JLabel title;
    private Rectangle titleRectangle;
    private static final MageVersion VERSION = new MageVersion(MageVersion.MAGE_VERSION_MAJOR, MageVersion.MAGE_VERSION_MINOR, MageVersion.MAGE_VERSION_PATCH, MageVersion.MAGE_VERSION_MINOR_PATCH, MageVersion.MAGE_VERSION_INFO);
    private UUID clientId;
    private static MagePane activeFrame;
    private static boolean liteMode = false;
    //TODO: make gray theme, implement theme selector in preferences dialog
    private static boolean grayMode = false;
    private static boolean fullscreenMode = false;

    private static final Map<UUID, ChatPanelBasic> CHATS = new HashMap<>();
    private static final Map<UUID, GamePanel> GAMES = new HashMap<>();
    private static final Map<UUID, DraftPanel> DRAFTS = new HashMap<>();
    private static final MageUI UI = new MageUI();

    private static final ScheduledExecutorService PING_TASK_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private static UpdateMemUsageTask updateMemUsageTask;

    private static long startTime;

    private final BalloonTip balloonTip;

    private List<CardInfo> missingCards;

    /**
     * @return the session
     */
    public static Session getSession() {
        return session;
    }

    public static JDesktopPane getDesktop() {
        return desktopPane;
    }

    public static Preferences getPreferences() {
        return PREFS;
    }

    public static boolean isLite() {
        return liteMode;
    }

    public static boolean isGray() {
        return grayMode;
    }

    @Override
    public MageVersion getVersion() {
        return VERSION;
    }

    public static MageFrame getInstance() {
        return instance;
    }

    /**
     * Creates new form MageFrame
     */
    public MageFrame() {
        setWindowTitle();

        clientId = UUID.randomUUID();
        EDTExceptionHandler.registerExceptionHandler();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApp();
            }
        });

        TConfig config = TConfig.current();
        config.setArchiveDetector(new TArchiveDetector("zip"));
        config.setAccessPreference(FsAccessOption.STORE, true);
        try {
            UIManager.put("desktop", new Color(0, 0, 0, 0));
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            GUISizeHelper.calculateGUISizes();
            // UIManager.put("Table.rowHeight", GUISizeHelper.tableRowHeight);
        } catch (Exception ex) {
            LOGGER.fatal(null, ex);
        }

        ManaSymbols.loadImages();
        Plugins.getInstance().loadPlugins();

        initComponents();

        desktopPane.setDesktopManager(new MageDesktopManager());

        setSize(1024, 768);
        SettingsManager.getInstance().setScreenWidthAndHeight(1024, 768);
        DialogManager.updateParams(768, 1024, false);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        session = new SessionImpl(this);
        callbackClient = new CallbackClientImpl(this);
        connectDialog = new ConnectDialog();
        desktopPane.add(connectDialog, JLayeredPane.POPUP_LAYER);
        errorDialog = new ErrorDialog();
        errorDialog.setLocation(100, 100);
        desktopPane.add(errorDialog, JLayeredPane.POPUP_LAYER);
        UI.addComponent(MageComponents.DESKTOP_PANE, desktopPane);

        PING_TASK_EXECUTOR.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                session.ping();
            }
        }, 60, 60, TimeUnit.SECONDS);

        updateMemUsageTask = new UpdateMemUsageTask(jMemUsageLabel);

        try {
            tablesPane = new TablesPane();
            desktopPane.add(tablesPane, javax.swing.JLayeredPane.DEFAULT_LAYER);
            tablesPane.setMaximum(true);
        } catch (PropertyVetoException ex) {
            LOGGER.fatal(null, ex);
        }

        addTooltipContainer();
        setBackground();
        addMageLabel();
        setAppIcon();
        MageTray.getInstance().install();

        desktopPane.add(ArrowBuilder.getBuilder().getArrowsManagerPanel(), JLayeredPane.DRAG_LAYER);

        desktopPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = ((JComponent) e.getSource()).getWidth();
                int height = ((JComponent) e.getSource()).getHeight();
                SettingsManager.getInstance().setScreenWidthAndHeight(width, height);
                if (!liteMode && !grayMode) {
                    backgroundPane.setSize(width, height);
                }

                ArrowBuilder.getBuilder().setSize(width, height);

                if (title != null) {
                    title.setBounds((int) (width - titleRectangle.getWidth()) / 2, (int) (height - titleRectangle.getHeight()) / 2, titleRectangle.width, titleRectangle.height);
                }
            }
        });

        // balloonTip = new BalloonTip(desktopPane, "", new ModernBalloonStyle(0, 0, Color.WHITE, Color.YELLOW, Color.BLUE), false);
        balloonTip = new BalloonTip(desktopPane, "", new EdgedBalloonStyle(Color.WHITE, Color.BLUE), false);
        balloonTip.setPositioner(new LeftAbovePositioner(0, 0));
        balloonTip.setVisible(false);

        mageToolbar.add(new javax.swing.JToolBar.Separator());
        mageToolbar.add(createWindowsButton());

        //TODO: move to plugin impl
        if (Plugins.getInstance().isCardPluginLoaded()) {
            Separator separator = new javax.swing.JToolBar.Separator();
            mageToolbar.add(separator);

            JButton btnDownloadSymbols = new JButton("Symbols");
            btnDownloadSymbols.setFocusable(false);
            btnDownloadSymbols.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            btnDownloadSymbols.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            btnDownloadSymbols.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnSymbolsActionPerformed(evt);
                }
            });
            mageToolbar.add(btnDownloadSymbols);

            separator = new javax.swing.JToolBar.Separator();
            mageToolbar.add(separator);

            JButton btnDownload = new JButton("Images");
            btnDownload.setFocusable(false);
            btnDownload.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            btnDownload.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            btnDownload.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnImagesActionPerformed(evt);
                }
            });
            mageToolbar.add(btnDownload);
        }

        if (Plugins.getInstance().isCounterPluginLoaded()) {
            int i = Plugins.getInstance().getGamesPlayed();
            JLabel label = new JLabel("  Games played: " + String.valueOf(i));
            desktopPane.add(label, JLayeredPane.DEFAULT_LAYER + 1);
            label.setVisible(true);
            label.setForeground(Color.white);
            label.setBounds(0, 0, 180, 30);
        }

        UI.addButton(MageComponents.TABLES_MENU_BUTTON, btnGames);

        setGUISize();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                disableButtons();
                if (PreferencesDialog.getCachedValue(PreferencesDialog.KEY_CARD_IMAGES_CHECK, "false").equals("true")) {
                    checkForNewImages();
                }

                updateMemUsageTask.execute();
                LOGGER.info("Client start up time: " + ((System.currentTimeMillis() - startTime) / 1000 + " seconds"));
                if (autoConnect()) {
                    enableButtons();
                } else {
                    connectDialog.showDialog();
                }
                setWindowTitle();

            }
        });

        if (SystemUtil.isMacOSX()) {
            SystemUtil.enableMacOSFullScreenMode(this);
            if (fullscreenMode) {
                SystemUtil.toggleMacOSFullScreenMode(this);
            }
        }
    }

    private void setWindowTitle() {
        setTitle(TITLE_NAME + "  Client: "
                + (VERSION == null ? "<not available>" : VERSION.toString()) + "  Server: "
                + ((session != null && session.isConnected()) ? session.getVersionInfo() : "<not connected>"));
    }

    private void addTooltipContainer() {
        final JEditorPane cardInfoPane = (JEditorPane) Plugins.getInstance().getCardInfoPane();
        if (cardInfoPane == null) {
            return;
        }
//        cardInfoPane.setSize(Constants.TOOLTIP_WIDTH_MIN, Constants.TOOLTIP_HEIGHT_MIN);
        cardInfoPane.setLocation(40, 40);
        cardInfoPane.setBackground(new Color(0, 0, 0, 0));

        MageRoundPane popupContainer = new MageRoundPane();
        popupContainer.setLayout(null);

        popupContainer.add(cardInfoPane);
        popupContainer.setVisible(false);
//        popupContainer.setBounds(0, 0,
//                Constants.TOOLTIP_WIDTH_MIN + Constants.TOOLTIP_BORDER_WIDTH,
//                Constants.TOOLTIP_HEIGHT_MIN + Constants.TOOLTIP_BORDER_WIDTH);

        desktopPane.add(popupContainer, JLayeredPane.POPUP_LAYER);

        UI.addComponent(MageComponents.CARD_INFO_PANE, cardInfoPane);
        UI.addComponent(MageComponents.POPUP_CONTAINER, popupContainer);
        // preview panel normal
        JPanel cardPreviewContainer = new JPanel();
        cardPreviewContainer.setOpaque(false);
        cardPreviewContainer.setLayout(null);
        BigCard bigCard = new BigCard();
        bigCard.setSize(320, 500);
        bigCard.setLocation(40, 40);
        bigCard.setBackground(new Color(0, 0, 0, 0));

        cardPreviewContainer.add(bigCard);
        cardPreviewContainer.setVisible(false);
        cardPreviewContainer.setBounds(0, 0, 320 + 80, 500 + 30);

        UI.addComponent(MageComponents.CARD_PREVIEW_PANE, bigCard);
        UI.addComponent(MageComponents.CARD_PREVIEW_CONTAINER, cardPreviewContainer);

        desktopPane.add(cardPreviewContainer, JLayeredPane.POPUP_LAYER);

        // preview panel rotated
        JPanel cardPreviewContainerRotated = new JPanel();
        cardPreviewContainerRotated.setOpaque(false);
        cardPreviewContainerRotated.setLayout(null);
        bigCard = new BigCard(true);
        bigCard.setSize(500, 350);
        bigCard.setLocation(40, 40);
        bigCard.setBackground(new Color(0, 0, 0, 0));
        cardPreviewContainerRotated.add(bigCard);
        cardPreviewContainerRotated.setVisible(false);
        cardPreviewContainerRotated.setBounds(0, 0, 500 + 80, 420 + 30);

        UI.addComponent(MageComponents.CARD_PREVIEW_PANE_ROTATED, bigCard);
        UI.addComponent(MageComponents.CARD_PREVIEW_CONTAINER_ROTATED, cardPreviewContainerRotated);

        desktopPane.add(cardPreviewContainerRotated, JLayeredPane.POPUP_LAYER);

    }

    private void setBackground() {
        if (liteMode || grayMode) {
            return;
        }
        String filename = "/background.jpg";
        try {
            if (Plugins.getInstance().isThemePluginLoaded()) {
                backgroundPane = (ImagePanel) Plugins.getInstance().updateTablePanel(new HashMap<String, JComponent>());
            } else {
                InputStream is = this.getClass().getResourceAsStream(filename);
                BufferedImage background = ImageIO.read(is);
                backgroundPane = new ImagePanel(background, ImagePanel.SCALED);
            }
            backgroundPane.setSize(1024, 768);
            desktopPane.add(backgroundPane, JLayeredPane.DEFAULT_LAYER);
        } catch (IOException e) {
            LOGGER.fatal("Error while setting background.", e);
        }
    }

    private void addMageLabel() {
        if (liteMode || grayMode) {
            return;
        }
        String filename = "/label-xmage.png";
        try {
            InputStream is = this.getClass().getResourceAsStream(filename);

            float ratio = 1179.0f / 678.0f;
            titleRectangle = new Rectangle(540, (int) (640 / ratio));
            if (is != null) {
                BufferedImage image = ImageIO.read(is);
                //ImageIcon resized = new ImageIcon(image.getScaledInstance(titleRectangle.width, titleRectangle.height, java.awt.Image.SCALE_SMOOTH));
                title = new JLabel();
                title.setIcon(new ImageIcon(image));
                backgroundPane.setLayout(null);
                backgroundPane.add(title);
            }
        } catch (IOException e) {
            LOGGER.fatal("Error while adding mage label.", e);
        }
    }

    private void setAppIcon() {
        Image image = ImageManagerImpl.getInstance().getAppImage();
        setIconImage(image);
    }

    private AbstractButton createWindowsButton() {
        final JToggleButton windowButton = new JToggleButton("Windows");
        windowButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    createAndShowMenu((JComponent) e.getSource(), windowButton);
                }
            }
        });
        windowButton.setFocusable(false);
        windowButton.setHorizontalTextPosition(SwingConstants.LEADING);
        return windowButton;
    }

    private void createAndShowMenu(final JComponent component, final AbstractButton windowButton) {
        JPopupMenu menu = new JPopupMenu();
        JInternalFrame[] windows = desktopPane.getAllFramesInLayer(javax.swing.JLayeredPane.DEFAULT_LAYER);
        MagePaneMenuItem menuItem;

        for (int i = 0; i < windows.length; i++) {
            if (windows[i] instanceof MagePane) {
                MagePane window = (MagePane) windows[i];
                if (window.isVisible()) {
                    menuItem = new MagePaneMenuItem(window);
                    menuItem.setFont(GUISizeHelper.menuFont);
                    menuItem.setState(i == 0);
                    menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            MagePane frame = ((MagePaneMenuItem) ae.getSource()).getFrame();
                            setActive(frame);
                        }
                    });
                    menuItem.setIcon(window.getFrameIcon());
                    menu.add(menuItem);
                }
            }
        }

        menu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                windowButton.setSelected(false);
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                windowButton.setSelected(false);
            }
        });

        menu.show(component, 0, component.getHeight());
    }

    private void checkForNewImages() {
        long beforeCall = System.currentTimeMillis();
        missingCards = CardRepository.instance.findCards(new CardCriteria());
        LOGGER.info("Card pool load time: " + ((System.currentTimeMillis() - beforeCall) / 1000 + " seconds"));
        beforeCall = System.currentTimeMillis();
        if (DownloadPictures.checkForNewCards(missingCards)) {
            LOGGER.info("Card images checking time: " + ((System.currentTimeMillis() - beforeCall) / 1000 + " seconds"));
            UserRequestMessage message = new UserRequestMessage("New images available", "Card images are missing (" + missingCards.size() + ").  Do you want to download the images?"
                    + "<br><br><i>You can deactivate the image download check on application start in the preferences.</i>");
            message.setButton1("No", null);
            message.setButton2("Yes", PlayerAction.CLIENT_DOWNLOAD_CARD_IMAGES);
            showUserRequestDialog(message);
        }
    }

    public void btnImagesActionPerformed(java.awt.event.ActionEvent evt) {
        List<CardInfo> cards = CardRepository.instance.findCards(new CardCriteria());
        DownloadPictures.startDownload(null, cards);
    }

    public void btnSymbolsActionPerformed(java.awt.event.ActionEvent evt) {
        UserRequestMessage message = new UserRequestMessage("Download additional resources", "Do you want to download game symbols and additional image files?");
        message.setButton1("No", null);
        message.setButton2("Yes", PlayerAction.CLIENT_DOWNLOAD_SYMBOLS);
        showUserRequestDialog(message);
    }

    public static void setActive(MagePane frame) {
        if (frame == null) {
            activeFrame = null;
            return;
        }
        LOGGER.debug("Setting " + frame.getTitle() + " active");
        if (activeFrame != null) {
            activeFrame.deactivated();
        }
        activeFrame = frame;
        activeFrame.setVisible(true);
        activeFrame.toFront();
        try {
            activeFrame.setSelected(true);
        } catch (PropertyVetoException ex) {
            LOGGER.error("Error setting " + frame.getTitle() + " active");
        }
        activeFrame.activated();
        ArrowBuilder.getBuilder().hideAllPanels();
        if (frame instanceof GamePane) {
            ArrowBuilder.getBuilder().showPanel(((GamePane) frame).getGameId());
            MusicPlayer.playBGM();
        } else {
            MusicPlayer.stopBGM();
        }
    }

    public static void deactivate(MagePane frame) {
        frame.setVisible(false);
        setActive(getTopMost(frame));
        if (activeFrame != frame) {
            frame.deactivated();
        }

    }

    private static MagePane getTopMost(MagePane exclude) {
        MagePane topmost = null;
        int best = Integer.MAX_VALUE;
        for (JInternalFrame frame : desktopPane.getAllFramesInLayer(JLayeredPane.DEFAULT_LAYER)) {
            if (frame.isVisible()) {
                int z = desktopPane.getComponentZOrder(frame);
                if (z < best) {
                    if (frame instanceof MagePane) {
                        best = z;
                        if (!frame.equals(exclude)) {
                            topmost = (MagePane) frame;
                        }
                    }
                }
            }
        }
        return topmost;
    }

    /**
     * Shows a game for a player of the game
     *
     * @param gameId
     * @param playerId
     */
    public void showGame(UUID gameId, UUID playerId) {
        try {
            GamePane gamePane = new GamePane();
            desktopPane.add(gamePane, JLayeredPane.DEFAULT_LAYER);
            gamePane.setMaximum(true);
            gamePane.setVisible(true);
            gamePane.showGame(gameId, playerId);
            setActive(gamePane);
        } catch (PropertyVetoException ex) {
        }
    }

    public void watchGame(UUID gameId) {
        try {
            for (Component component : desktopPane.getComponents()) {
                if (component instanceof GamePane
                        && ((GamePane) component).getGameId().equals(gameId)) {
                    setActive((GamePane) component);
                    return;
                }
            }
            GamePane gamePane = new GamePane();
            desktopPane.add(gamePane, JLayeredPane.DEFAULT_LAYER);
            gamePane.setMaximum(true);
            gamePane.setVisible(true);
            gamePane.watchGame(gameId);
            setActive(gamePane);
        } catch (PropertyVetoException ex) {
            LOGGER.debug("Problem starting watching game " + gameId, ex);
        }
    }

    public void replayGame(UUID gameId) {
        try {
            GamePane gamePane = new GamePane();
            desktopPane.add(gamePane, JLayeredPane.DEFAULT_LAYER);
            gamePane.setMaximum(true);
            gamePane.setVisible(true);
            gamePane.replayGame(gameId);
            setActive(gamePane);
        } catch (PropertyVetoException ex) {
        }
    }

    public void showDraft(UUID draftId) {
        try {
            DraftPane draftPane = new DraftPane();
            desktopPane.add(draftPane, JLayeredPane.DEFAULT_LAYER);
            draftPane.setMaximum(true);
            draftPane.setVisible(true);
            draftPane.showDraft(draftId);
            setActive(draftPane);
        } catch (PropertyVetoException ex) {
        }
    }

    public void endDraft(UUID draftId) {
        // inform all open draft panes about
        for (JInternalFrame window : desktopPane.getAllFramesInLayer(JLayeredPane.DEFAULT_LAYER)) {
            if (window instanceof DraftPane) {
                DraftPane draftPane = (DraftPane) window;
                draftPane.removeDraft();
            }
        }
    }

    public void showTournament(UUID tournamentId) {
        try {
            for (Component component : desktopPane.getComponents()) {
                if (component instanceof TournamentPane
                        && ((TournamentPane) component).getTournamentId().equals(tournamentId)) {
                    setActive((TournamentPane) component);
                    return;
                }
            }
            TournamentPane tournamentPane = new TournamentPane();
            desktopPane.add(tournamentPane, JLayeredPane.DEFAULT_LAYER);
            tournamentPane.setMaximum(true);
            tournamentPane.setVisible(true);
            tournamentPane.showTournament(tournamentId);
            setActive(tournamentPane);
        } catch (PropertyVetoException ex) {
        }
    }

    public void showGameEndDialog(GameEndView gameEndView) {
        GameEndDialog gameEndDialog = new GameEndDialog(gameEndView);
        desktopPane.add(gameEndDialog, JLayeredPane.MODAL_LAYER);
        gameEndDialog.showDialog();
    }

    public void showTableWaitingDialog(UUID roomId, UUID tableId, boolean isTournament) {
        TableWaitingDialog tableWaitingDialog = new TableWaitingDialog();
        desktopPane.add(tableWaitingDialog, JLayeredPane.MODAL_LAYER);
        tableWaitingDialog.showDialog(roomId, tableId, isTournament);
    }

    public static boolean connect(Connection connection) {
        boolean result = session.connect(connection);
        MageFrame.getInstance().setWindowTitle();
        return result;
    }

    public static boolean stopConnecting() {
        return session.stopConnecting();
    }

    public boolean autoConnect() {
        boolean autoConnectParamValue = Boolean.parseBoolean(PREFS.get("autoConnect", "false"));
        boolean status = false;
        if (autoConnectParamValue) {
            status = performConnect();
        }
        return status;
    }

    private boolean performConnect() {
        String server = MagePreferences.getServerAddress();
        int port = MagePreferences.getServerPort();
        String userName = MagePreferences.getUserName(server);
        String password = MagePreferences.getPassword(server);
        String proxyServer = PREFS.get("proxyAddress", "");
        int proxyPort = Integer.parseInt(PREFS.get("proxyPort", "0"));
        ProxyType proxyType = ProxyType.valueByText(PREFS.get("proxyType", "None"));
        String proxyUsername = PREFS.get("proxyUsername", "");
        String proxyPassword = PREFS.get("proxyPassword", "");
        try {
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            Connection connection = new Connection();
            connection.setUsername(userName);
            connection.setPassword(password);
            connection.setHost(server);
            connection.setPort(port);
            connection.setProxyType(proxyType);
            connection.setProxyHost(proxyServer);
            connection.setProxyPort(proxyPort);
            connection.setProxyUsername(proxyUsername);
            connection.setProxyPassword(proxyPassword);

            setUserPrefsToConnection(connection);

            LOGGER.debug("connecting (auto): " + proxyType + " " + proxyServer + " " + proxyPort + " " + proxyUsername);
            if (MageFrame.connect(connection)) {
                showGames(false);
                return true;
            } else {
                showMessage("Unable to connect to server");
            }
        } finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
        return false;
    }

    public void setUserPrefsToConnection(Connection connection) {
        connection.setUserData(PreferencesDialog.getUserData());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        desktopPane = new MageJDesktop();
        mageToolbar = new javax.swing.JToolBar();
        btnSendFeedback = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        btnConnect = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        btnGames = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        btnDeckEditor = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        btnCollectionViewer = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        btnPreferences = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        btnAbout = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        btnExit = new javax.swing.JButton();
        jMemUsageLabel = new javax.swing.JLabel();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        lblStatus = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1024, 768));

        desktopPane.setBackground(new java.awt.Color(204, 204, 204));

        mageToolbar.setFloatable(false);
        mageToolbar.setRollover(true);
        mageToolbar.setFont(new java.awt.Font("Segoe UI", 0, 48)); // NOI18N
        mageToolbar.setMaximumSize(new java.awt.Dimension(614, 60));
        mageToolbar.setMinimumSize(new java.awt.Dimension(566, 60));
        mageToolbar.setPreferredSize(new java.awt.Dimension(614, 60));

        btnSendFeedback.setText("Feedback");
        btnSendFeedback.setFocusable(false);
        btnSendFeedback.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSendFeedback.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSendFeedback.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendFeedbackActionPerformed(evt);
            }
        });
        mageToolbar.add(btnSendFeedback);
        mageToolbar.add(jSeparator4);

        btnConnect.setText("Connect");
        btnConnect.setFocusable(false);
        btnConnect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnConnect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });
        mageToolbar.add(btnConnect);
        mageToolbar.add(jSeparator3);

        btnGames.setText("Games");
        btnGames.setFocusable(false);
        btnGames.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnGames.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnGames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGamesActionPerformed(evt);
            }
        });
        mageToolbar.add(btnGames);
        mageToolbar.add(jSeparator1);

        btnDeckEditor.setText("Deck Editor");
        btnDeckEditor.setFocusable(false);
        btnDeckEditor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDeckEditor.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDeckEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeckEditorActionPerformed(evt);
            }
        });
        mageToolbar.add(btnDeckEditor);
        mageToolbar.add(jSeparator2);

        btnCollectionViewer.setText("Collection Viewer");
        btnCollectionViewer.setFocusable(false);
        btnCollectionViewer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCollectionViewer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCollectionViewer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCollectionViewerActionPerformed(evt);
            }
        });
        mageToolbar.add(btnCollectionViewer);
        mageToolbar.add(jSeparator5);

        btnPreferences.setText("Preferences");
        btnPreferences.setFocusable(false);
        btnPreferences.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnPreferences.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnPreferences.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreferencesActionPerformed(evt);
            }
        });
        mageToolbar.add(btnPreferences);
        mageToolbar.add(jSeparator6);

        btnAbout.setText("About");
        btnAbout.setFocusable(false);
        btnAbout.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAbout.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAboutActionPerformed(evt);
            }
        });
        mageToolbar.add(btnAbout);
        mageToolbar.add(jSeparator7);

        btnExit.setText("Exit");
        btnExit.setFocusable(false);
        btnExit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });
        mageToolbar.add(btnExit);

        jMemUsageLabel.setText("100% Free mem");
        mageToolbar.add(Box.createHorizontalGlue());
        mageToolbar.add(jMemUsageLabel);
        mageToolbar.add(jSeparator8);

        lblStatus.setText("Not connected ");
        mageToolbar.add(lblStatus);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(desktopPane)
            .addComponent(mageToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 697, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(mageToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(desktopPane, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDeckEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeckEditorActionPerformed
        showDeckEditor(DeckEditorMode.FREE_BUILDING, null, null, 0);
    }//GEN-LAST:event_btnDeckEditorActionPerformed

    private void btnGamesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGamesActionPerformed
        this.showGames(true);
    }//GEN-LAST:event_btnGamesActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        exitApp();
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        if (session.isConnected()) {
            UserRequestMessage message = new UserRequestMessage("Confirm disconnect", "Are you sure you want to disconnect?");
            message.setButton1("No", null);
            message.setButton2("Yes", PlayerAction.CLIENT_DISCONNECT);
            showUserRequestDialog(message);
        } else {
            connectDialog.showDialog();
            setWindowTitle();
        }
    }//GEN-LAST:event_btnConnectActionPerformed

    public void btnAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAboutActionPerformed
        JInternalFrame[] windows = desktopPane.getAllFramesInLayer(JLayeredPane.POPUP_LAYER);
        for (JInternalFrame window : windows) {
            if (window instanceof AboutDialog) {
                // don't open the window twice.
                return;
            }
        }
        AboutDialog aboutDialog = new AboutDialog();
        desktopPane.add(aboutDialog, JLayeredPane.POPUP_LAYER);
        aboutDialog.showDialog(VERSION);
    }//GEN-LAST:event_btnAboutActionPerformed

    private void btnCollectionViewerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCollectionViewerActionPerformed
        showCollectionViewer();
    }//GEN-LAST:event_btnCollectionViewerActionPerformed

    public void btnPreferencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreferencesActionPerformed
        PreferencesDialog.main(new String[]{});
    }//GEN-LAST:event_btnPreferencesActionPerformed

    public void btnSendFeedbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendFeedbackActionPerformed
        if (!session.isConnected()) {
            JOptionPane.showMessageDialog(null, "You may send us feedback only when connected to server.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        FeedbackDialog.main(new String[]{});
    }//GEN-LAST:event_btnSendFeedbackActionPerformed

    public void exitApp() {
        if (session.isConnected()) {
            UserRequestMessage message = new UserRequestMessage("Confirm disconnect", "You are currently connected.  Are you sure you want to disconnect?");
            message.setButton1("No", null);
            message.setButton2("Yes", PlayerAction.CLIENT_EXIT);
            MageFrame.getInstance().showUserRequestDialog(message);
        } else {
            UserRequestMessage message = new UserRequestMessage("Confirm exit", "Are you sure you want to exit?");
            message.setButton1("No", null);
            message.setButton2("Yes", PlayerAction.CLIENT_EXIT);
            MageFrame.getInstance().showUserRequestDialog(message);
        }
    }

    public void enableButtons() {
        btnConnect.setEnabled(true);
        btnConnect.setText("Disconnect");
        btnGames.setEnabled(true);
        btnDeckEditor.setEnabled(true);
    }

    public void disableButtons() {
        btnConnect.setEnabled(true);
        btnConnect.setText("Connect");
        btnGames.setEnabled(false);
        btnDeckEditor.setEnabled(true);
    }

    public void hideTables() {
        this.tablesPane.hideTables();
    }

    public void showGames(boolean setActive) {
        MagePane topPanebefore = getTopMost(tablesPane);
        if (!tablesPane.isVisible()) {
            this.tablesPane.setVisible(true);
            this.tablesPane.showTables();
        }
        if (setActive) {
            setActive(tablesPane);
        } else // if other panel was already shown, mamke sure it's topmost again
        {
            if (topPanebefore != null) {
                setActive(topPanebefore);
            }
        }
    }

    public void hideGames() {
        JInternalFrame[] windows = desktopPane.getAllFramesInLayer(JLayeredPane.DEFAULT_LAYER);
        for (JInternalFrame window : windows) {
            if (window instanceof GamePane) {
                GamePane gamePane = (GamePane) window;
                gamePane.removeGame();
            }
            if (window instanceof DraftPane) {
                DraftPane draftPane = (DraftPane) window;
                draftPane.removeDraft();
            }
            if (window instanceof TournamentPane) {
                TournamentPane tournamentPane = (TournamentPane) window;
                tournamentPane.removeTournament();
            }
            // close & remove sideboarding or construction pane if open
            if (window instanceof DeckEditorPane) {
                DeckEditorPane deckEditorPane = (DeckEditorPane) window;
                if (deckEditorPane.getDeckEditorMode().equals(DeckEditorMode.LIMITED_BUILDING)
                        || deckEditorPane.getDeckEditorMode().equals(DeckEditorMode.SIDEBOARDING)) {
                    deckEditorPane.removeFrame();
                }
            }

        }
    }

    public void showDeckEditor(DeckEditorMode mode, Deck deck, UUID tableId, int time) {
        String name;
        if (mode == DeckEditorMode.SIDEBOARDING || mode == DeckEditorMode.LIMITED_BUILDING) {
            name = "Deck Editor - " + tableId.toString();
        } else {
            if (deck != null) {
                name = "Deck Editor - " + deck.getName();
            } else {
                name = "Deck Editor";
            }
            // use already open editor
            JInternalFrame[] windows = desktopPane.getAllFramesInLayer(JLayeredPane.DEFAULT_LAYER);
            for (JInternalFrame window : windows) {
                if (window instanceof DeckEditorPane && window.getTitle().equals(name)) {
                    setActive((MagePane) window);
                    return;
                }
            }
        }

        try {
            DeckEditorPane deckEditorPane = new DeckEditorPane();
            desktopPane.add(deckEditorPane, JLayeredPane.DEFAULT_LAYER);
            deckEditorPane.setMaximum(true);
            deckEditorPane.setVisible(true);
            deckEditorPane.show(mode, deck, name, tableId, time);
            setActive(deckEditorPane);
        } catch (PropertyVetoException ex) {
            LOGGER.fatal(null, ex);
        }
    }

    public void showUserRequestDialog(final UserRequestMessage userRequestMessage) {
        final UserRequestDialog userRequestDialog = new UserRequestDialog();
        userRequestDialog.setLocation(100, 100);
        desktopPane.add(userRequestDialog, JLayeredPane.POPUP_LAYER);
        if (SwingUtilities.isEventDispatchThread()) {
            userRequestDialog.showDialog(userRequestMessage);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    userRequestDialog.showDialog(userRequestMessage);
                }
            });
        }

    }

    public void showErrorDialog(final String title, final String message) {
        if (SwingUtilities.isEventDispatchThread()) {
            errorDialog.showDialog(title, message);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    errorDialog.showDialog(title, message);
                }
            });
        }

    }

    public void showCollectionViewer() {
        JInternalFrame[] windows = desktopPane.getAllFramesInLayer(JLayeredPane.DEFAULT_LAYER);
        for (JInternalFrame window : windows) {
            if (window instanceof CollectionViewerPane) {
                setActive((MagePane) window);
                return;
            }
        }
        try {
            CollectionViewerPane collectionViewerPane = new CollectionViewerPane();
            desktopPane.add(collectionViewerPane, javax.swing.JLayeredPane.DEFAULT_LAYER);
            collectionViewerPane.setMaximum(true);
            collectionViewerPane.setVisible(true);
            setActive(collectionViewerPane);
        } catch (PropertyVetoException ex) {
            LOGGER.fatal(null, ex);
        }
    }

    static void renderSplashFrame(Graphics2D g) {
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(120, 140, 200, 40);
        g.setPaintMode();
        g.setColor(Color.white);
        g.drawString("Version 0.6.1", 560, 460);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(final String args[]) {
        // Workaround for #451
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        LOGGER.info("Starting MAGE client version " + VERSION);
        LOGGER.info("Logging level: " + LOGGER.getEffectiveLevel());

        startTime = System.currentTimeMillis();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOGGER.fatal(null, e);
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (String arg : args) {
                    if (arg.startsWith(LITE_MODE_ARG)) {
                        liteMode = true;
                    }
                    if (arg.startsWith(GRAY_MODE_ARG)) {
                        grayMode = true;
                    }
                    if (arg.startsWith(FILL_SCREEN_ARG)) {
                        fullscreenMode = true;
                    }
                }
                if (!liteMode) {
                    final SplashScreen splash = SplashScreen.getSplashScreen();
                    if (splash != null) {
                        Graphics2D g = splash.createGraphics();
                        if (g != null) {
                            renderSplashFrame(g);
                        }
                        splash.update();
                    }
                }
                instance = new MageFrame();
                instance.setVisible(true);

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAbout;
    private javax.swing.JButton btnCollectionViewer;
    private javax.swing.JButton btnConnect;
    private javax.swing.JButton btnDeckEditor;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnGames;
    private javax.swing.JButton btnPreferences;
    private javax.swing.JButton btnSendFeedback;
    private static javax.swing.JDesktopPane desktopPane;
    private javax.swing.JLabel jMemUsageLabel;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JToolBar.Separator jSeparator8;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JToolBar mageToolbar;
    // End of variables declaration//GEN-END:variables

    private static final long serialVersionUID = -9104885239063142218L;
    private ImagePanel backgroundPane;
    private TablesPane tablesPane;
//    private CollectionViewerPane collectionViewerPane;

    public void setStatusText(String status) {
        this.lblStatus.setText(status);
        changeGUISize(); // Needed to layout the tooltbar after text length chnage
    }

    public static MageUI getUI() {
        return UI;
    }

    public static ChatPanelBasic getChat(UUID chatId) {
        return CHATS.get(chatId);
    }

    public static void addChat(UUID chatId, ChatPanelBasic chatPanel) {
        CHATS.put(chatId, chatPanel);
    }

    public static void removeChat(UUID chatId) {
        CHATS.remove(chatId);
    }

    public static Map<UUID, ChatPanelBasic> getChatPanels() {
        return CHATS;
    }

    public static void addGame(UUID gameId, GamePanel gamePanel) {
        GAMES.put(gameId, gamePanel);
    }

    public static GamePanel getGame(UUID gameId) {
        return GAMES.get(gameId);
    }

    public static void removeGame(UUID gameId) {
        GAMES.remove(gameId);
    }

    public static DraftPanel getDraft(UUID draftId) {
        return DRAFTS.get(draftId);
    }

    public static void removeDraft(UUID draftId) {
        DraftPanel draftPanel = DRAFTS.get(draftId);
        if (draftPanel != null) {
            DRAFTS.remove(draftId);
            draftPanel.hideDraft();
        }
    }

    public static void addDraft(UUID draftId, DraftPanel draftPanel) {
        DRAFTS.put(draftId, draftPanel);
    }

    public BalloonTip getBalloonTip() {
        return balloonTip;
    }

    @Override
    public void connected(final String message) {
        if (SwingUtilities.isEventDispatchThread()) {
            setStatusText(message);
            enableButtons();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setStatusText(message);
                    enableButtons();
                }
            });
        }
    }

    @Override
    public void disconnected(final boolean errorCall) {
        if (SwingUtilities.isEventDispatchThread()) { // Returns true if the current thread is an AWT event dispatching thread.
            LOGGER.info("DISCONNECTED (Event Dispatch Thread)");
            setStatusText("Not connected");
            disableButtons();
            hideGames();
            hideTables();
        } else {
            LOGGER.info("DISCONNECTED (NO Event Dispatch Thread)");
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setStatusText("Not connected");
                    disableButtons();
                    hideGames();
                    hideTables();
                    if (errorCall) {
                        UserRequestMessage message = new UserRequestMessage("Connection lost", "The connection to server was lost. Reconnect?");
                        message.setButton1("No", null);
                        message.setButton2("Yes", PlayerAction.CLIENT_RECONNECT);
                        showUserRequestDialog(message);
                    } else {
                        session.disconnect(false);
                    }
                }
            }
            );
        }
    }

    @Override
    public void showMessage(String message) {
        final UserRequestMessage requestMessage = new UserRequestMessage("Message", message);
        requestMessage.setButton1("OK", null);
        MageFrame.getInstance().showUserRequestDialog(requestMessage);
    }

    @Override
    public void showError(final String message) {
        final UserRequestMessage requestMessage = new UserRequestMessage("Error", message);
        requestMessage.setButton1("OK", null);
        MageFrame.getInstance().showUserRequestDialog(requestMessage);
    }

    @Override
    public void processCallback(ClientCallback callback) {
        callbackClient.processCallback(callback);
    }

    public void sendUserReplay(PlayerAction playerAction, UserRequestMessage userRequestMessage) {
        switch (playerAction) {
            case CLIENT_DOWNLOAD_SYMBOLS:
                Plugins.getInstance().downloadSymbols();
                break;
            case CLIENT_DOWNLOAD_CARD_IMAGES:
                DownloadPictures.startDownload(null, missingCards);
                break;
            case CLIENT_DISCONNECT:
                session.disconnect(false);
                tablesPane.clearChat();
                showMessage("You have disconnected");
                setWindowTitle();
                break;
            case CLIENT_QUIT_TOURNAMENT:
                MageFrame.getSession().quitTournament(userRequestMessage.getTournamentId());
                break;
            case CLIENT_QUIT_DRAFT_TOURNAMENT:
                MageFrame.getSession().quitDraft(userRequestMessage.getTournamentId());
                MageFrame.removeDraft(userRequestMessage.getTournamentId());
                break;
            case CLIENT_CONCEDE_GAME:
                MageFrame.getSession().sendPlayerAction(PlayerAction.CONCEDE, userRequestMessage.getGameId(), null);
                break;
            case CLIENT_CONCEDE_MATCH:
                MageFrame.getSession().quitMatch(userRequestMessage.getGameId());
                break;
            case CLIENT_STOP_WATCHING:
                session.stopWatching(userRequestMessage.getGameId());
                GamePanel gamePanel = getGame(userRequestMessage.getGameId());
                if (gamePanel != null) {
                    gamePanel.removeGame();
                }
                removeGame(userRequestMessage.getGameId());
                break;
            case CLIENT_EXIT:
                if (session.isConnected()) {
                    session.disconnect(false);
                }
                CardRepository.instance.closeDB();
                tablesPane.cleanUp();
                Plugins.getInstance().shutdown();
                dispose();
                System.exit(0);
                break;
            case CLIENT_REMOVE_TABLE:
                session.removeTable(userRequestMessage.getRoomId(), userRequestMessage.getTableId());
                break;
            case CLIENT_RECONNECT:
                if (performConnect()) {
                    enableButtons();
                }
                break;
            case CLIENT_REPLAY_ACTION:
                session.stopReplay(userRequestMessage.getGameId());
                break;
            default:
                if (session != null && playerAction != null) {
                    session.sendPlayerAction(playerAction, userRequestMessage.getGameId(), userRequestMessage.getRelatedUserId());
                }

        }
    }

    public void changeGUISize() {
        setGUISize();
        Plugins.getInstance().changeGUISize();
        CountryUtil.changeGUISize();
        for (Component component : desktopPane.getComponents()) {
            if (component instanceof MageDialog) {
                ((MageDialog) component).changeGUISize();
            }
            if (component instanceof MagePane) {
                ((MagePane) component).changeGUISize();
            }
        }
        for (ChatPanelBasic chatPanel : getChatPanels().values()) {
            chatPanel.changeGUISize(GUISizeHelper.chatFont);
        }
        try {
            CardInfoPaneImpl cardInfoPane = (CardInfoPaneImpl) UI.getComponent(MageComponents.CARD_INFO_PANE);
            if (cardInfoPane != null) {
                cardInfoPane.changeGUISize();
            }

        } catch (Exception ex) {

        }

        this.revalidate();
        this.repaint();
    }

    private void setGUISize() {
        Font font = GUISizeHelper.menuFont;
        mageToolbar.setFont(font);
        int newHeight = font.getSize() + 6;
        Dimension mageToolbarDimension = mageToolbar.getPreferredSize();
        mageToolbarDimension.height = newHeight + 6;
        mageToolbar.setMinimumSize(mageToolbarDimension);
        mageToolbar.setMaximumSize(mageToolbarDimension);
        mageToolbar.setPreferredSize(mageToolbarDimension);
        for (Component component : mageToolbar.getComponents()) {
            if (component instanceof JButton || component instanceof JLabel || component instanceof JToggleButton) {
                component.setFont(font);
                Dimension d = component.getPreferredSize();
                d.height = newHeight;
                component.setMinimumSize(d);
                component.setMaximumSize(d);

            }
            if (component instanceof javax.swing.JToolBar.Separator) {
                Dimension d = component.getPreferredSize();
                d.height = newHeight;
                component.setMinimumSize(d);
                component.setMaximumSize(d);
            }
        }
        balloonTip.setFont(GUISizeHelper.balloonTooltipFont);
    }
}

class MagePaneMenuItem extends JCheckBoxMenuItem {

    private final MagePane frame;

    public MagePaneMenuItem(MagePane frame) {
        super(frame.getTitle());
        this.frame = frame;
    }

    public MagePane getFrame() {
        return frame;
    }
}
