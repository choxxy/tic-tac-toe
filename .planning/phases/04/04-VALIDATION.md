# Phase 4 Validation: LAN Multiplayer

## Success Criteria
1. [ ] **Low Friction:** LAN game connection starts within 3 taps and <5 seconds discovery.
2. [ ] **Stability:** No crashes on sudden LAN disconnect.
3. [ ] **Recovery:** Game state is preserved and resumes after a brief network interruption.

## Automated Verification
- [ ] `GameMessageTest` passes (Serialization)
- [ ] `NsdDiscoveryManagerTest` passes (Discovery Flow)
- [ ] `GameSocketManagerTest` passes (TCP I/O)
- [ ] `LanLobbyViewModelTest` passes (Handshake logic)
- [ ] `GameViewModelNetworkTest` passes (Move synchronization)
- [ ] `GameDisconnectDetectionTest` passes (Timeout logic)

## Manual User Acceptance (UAT)
| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Open LAN Lobby on two devices | Both show "Host Game" / "Join Game" tabs |
| 2 | Device A: Click "Host", Device B: Click "Join" | Device A shows in Device B's list |
| 3 | Device B: Tap Device A in list | Both devices navigate to Game screen with correct names |
| 4 | Play a game | Moves are synchronized instantly |
| 5 | Toggle Wifi on one device | "Reconnecting..." overlay appears on both |
| 6 | Toggle Wifi back on | Overlay disappears, game resumes where it left off |
| 7 | Disconnect and wait 10s | Recovery options (CPU/Exit) appear |
