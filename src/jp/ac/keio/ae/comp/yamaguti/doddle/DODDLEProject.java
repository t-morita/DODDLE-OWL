 p a c k a g e   j p . a c . k e i o . a e . c o m p . y a m a g u t i . d o d d l e ;  
  
 i m p o r t   j a v a . a w t . * ;  
 i m p o r t   j a v a . a w t . e v e n t . * ;  
 i m p o r t   j a v a . b e a n s . * ;  
 i m p o r t   j a v a . u t i l . * ;  
  
 i m p o r t   j a v a x . s w i n g . * ;  
 i m p o r t   j a v a x . s w i n g . e v e n t . * ;  
  
 i m p o r t   j p . a c . k e i o . a e . c o m p . y a m a g u t i . d o d d l e . d a t a . * ;  
 i m p o r t   j p . a c . k e i o . a e . c o m p . y a m a g u t i . d o d d l e . u i . * ;  
 i m p o r t   j p . a c . k e i o . a e . c o m p . y a m a g u t i . d o d d l e . u t i l s . * ;  
  
 / * *  
   *   @ a u t h o r   t a k e s h i   m o r i t a  
   * /  
 p u b l i c   c l a s s   D O D D L E P r o j e c t   e x t e n d s   J I n t e r n a l F r a m e   i m p l e m e n t s   A c t i o n L i s t e n e r   {  
  
         p r i v a t e   J T a b b e d P a n e   t a b b e d P a n e ;  
         p r i v a t e   O n t o l o g y S e l e c t i o n P a n e l   o n t S e l e c t i o n P a n e l ;  
         p r i v a t e   D o c u m e n t S e l e c t i o n P a n e l   d o c S e l e c t i o n P a n e l ;  
         p r i v a t e   I n p u t W o r d S e l e c t i o n P a n e l   i n p u t W o r d S e l e c t i n P a n e l ;  
         p r i v a t e   I n p u t M o d u l e U I   i n p u t M o d u l e U I ;  
         p r i v a t e   C o n s t r u c t C o n c e p t T r e e P a n e l   c o n s t r u c t C o n c e p t T r e e P a n e l ;  
         p r i v a t e   C o n s t r u c t P r o p e r t y T r e e P a n e l   c o n s t r u c t P r o p e r t y T r e e P a n e l ;  
         p r i v a t e   T e x t C o n c e p t D e f i n i t i o n P a n e l   t e x t C o n c e p t D e f i n i t i o n P a n e l ;  
  
         p r i v a t e   i n t   u s e r I D C o u n t ;  
         p r i v a t e   M a p < S t r i n g ,   C o n c e p t >   i d C o n c e p t M a p ;  
  
         p r i v a t e   J M e n u   p r o j e c t M e n u ;  
         p r i v a t e   J C h e c k B o x M e n u I t e m   p r o j e c t M e n u I t e m ;  
  
         p u b l i c   D O D D L E P r o j e c t ( S t r i n g   t i t l e ,   J M e n u   p m )   {  
                 s u p e r ( t i t l e ,   t r u e ,   t r u e ,   t r u e ,   t r u e ) ;  
                 p r o j e c t M e n u   =   p m ;  
                 p r o j e c t M e n u I t e m   =   n e w   J C h e c k B o x M e n u I t e m ( t i t l e ) ;  
                 p r o j e c t M e n u I t e m . a d d A c t i o n L i s t e n e r ( t h i s ) ;  
                 p r o j e c t M e n u . a d d ( p r o j e c t M e n u I t e m ) ;  
  
                 u s e r I D C o u n t   =   0 ;  
                 i d C o n c e p t M a p   =   n e w   H a s h M a p < S t r i n g ,   C o n c e p t > ( ) ;  
                 c o n s t r u c t C o n c e p t T r e e P a n e l   =   n e w   C o n s t r u c t C o n c e p t T r e e P a n e l ( t h i s ) ;  
                 o n t S e l e c t i o n P a n e l   =   n e w   O n t o l o g y S e l e c t i o n P a n e l ( ) ;  
                 c o n s t r u c t P r o p e r t y T r e e P a n e l   =   n e w   C o n s t r u c t P r o p e r t y T r e e P a n e l ( t h i s ) ;  
                 i n p u t M o d u l e U I   =   n e w   I n p u t M o d u l e U I ( c o n s t r u c t C o n c e p t T r e e P a n e l ,   c o n s t r u c t P r o p e r t y T r e e P a n e l ,   t h i s ) ;  
                 i n p u t W o r d S e l e c t i n P a n e l   =   n e w   I n p u t W o r d S e l e c t i o n P a n e l ( i n p u t M o d u l e U I ) ;  
                 d o c S e l e c t i o n P a n e l   =   n e w   D o c u m e n t S e l e c t i o n P a n e l ( i n p u t W o r d S e l e c t i n P a n e l ) ;  
                 t e x t C o n c e p t D e f i n i t i o n P a n e l   =   n e w   T e x t C o n c e p t D e f i n i t i o n P a n e l ( t h i s ) ;  
                 i n p u t M o d u l e U I . s e t D o c u m e n t S e l e c t i o n P a n e l ( d o c S e l e c t i o n P a n e l ) ;  
                 t a b b e d P a n e   =   n e w   J T a b b e d P a n e ( ) ;  
                 t a b b e d P a n e . a d d T a b ( "0񠐤򼡸�0鼝xb� " ,   U t i l s . g e t I m a g e I c o n ( " o n t o l o g y . p n g " ) ,   o n t S e l e c t i o n P a n e l ) ;  
                 t a b b e d P a n e . a d d T a b ( "e噁鴲xb� " ,   U t i l s . g e t I m a g e I c o n ( " o p e n _ d o c . g i f " ) ,   d o c S e l e c t i o n P a n e l ) ;  
                 t a b b e d P a n e . a d d T a b ( "QeR汼X姙恱b� " ,   U t i l s . g e t I m a g e I c o n ( " i n p u t _ w o r d s . p n g " ) ,   i n p u t W o r d S e l e c t i n P a n e l ) ;  
                 t a b b e d P a n e . a d d T a b ( "Yー'夈m� " ,   U t i l s . g e t I m a g e I c o n ( " d i s a m b i g u a t i o n . p n g " ) ,   i n p u t M o d u l e U I ) ;  
                 t a b b e d P a n e . a d d T a b ( "0񯧘箹嶾di藍� " ,   U t i l s . g e t I m a g e I c o n ( " c l a s s _ t r e e . p n g " ) ,   c o n s t r u c t C o n c e p t T r e e P a n e l ) ;  
                 t a b b e d P a n e . a d d T a b ( "0󪫀󘇪嶾di藍� " ,   U t i l s . g e t I m a g e I c o n ( " p r o p e r t y _ t r e e . p n g " ) ,   c o n s t r u c t P r o p e r t y T r e e P a n e l ) ;  
                 t a b b e d P a n e . a d d T a b ( "i俖鮗�� " ,   U t i l s . g e t I m a g e I c o n ( " n o n - t a x o n o m i c . p n g " ) ,   t e x t C o n c e p t D e f i n i t i o n P a n e l ) ;  
                 g e t C o n t e n t P a n e ( ) . a d d ( t a b b e d P a n e ,   B o r d e r L a y o u t . C E N T E R ) ;  
                 s e t D e f a u l t C l o s e O p e r a t i o n ( J F r a m e . E X I T _ O N _ C L O S E ) ;  
                 a d d I n t e r n a l F r a m e L i s t e n e r ( n e w   I n t e r n a l F r a m e A d a p t e r ( )   {  
                         p u b l i c   v o i d   i n t e r n a l F r a m e C l o s i n g ( I n t e r n a l F r a m e E v e n t   e )   {  
                                 i n t   m e s s a g e T y p e   =   J O p t i o n P a n e . s h o w C o n f i r m D i a l o g ( t a b b e d P a n e ,   g e t T i t l e ( )   +   " \ n0󪫀򋈼񯢎拀BN�0W0~0Y0K� " ) ;  
                                 i f   ( m e s s a g e T y p e   = =   J O p t i o n P a n e . Y E S _ O P T I O N )   {  
                                         p r o j e c t M e n u . r e m o v e ( p r o j e c t M e n u I t e m ) ;  
                                         d i s p o s e ( ) ;  
                                 }  
                         }  
                 } ) ;  
                 s e t S i z e ( 6 0 0 ,   5 0 0 ) ;  
         }  
  
         p u b l i c   v o i d   s e t P r o j e c t N a m e ( S t r i n g   n a m e )   {  
                 p r o j e c t M e n u I t e m . s e t T e x t ( n a m e ) ;  
         }  
  
         p u b l i c   v o i d   a c t i o n P e r f o r m e d ( A c t i o n E v e n t   e )   {  
                 i f   ( e . g e t S o u r c e ( )   = =   p r o j e c t M e n u I t e m )   {  
                         f o r   ( i n t   i   =   0 ;   i   <   p r o j e c t M e n u . g e t I t e m C o u n t ( ) ;   i + + )   {  
                                 J C h e c k B o x M e n u I t e m   i t e m   =   ( J C h e c k B o x M e n u I t e m )   p r o j e c t M e n u . g e t I t e m ( i ) ;  
                                 i t e m . s e t S e l e c t e d ( f a l s e ) ;  
                         }  
                         p r o j e c t M e n u I t e m . s e t S e l e c t e d ( t r u e ) ;  
                         t o F r o n t ( ) ;  
                         t r y   {  
                                 s e t S e l e c t e d ( t r u e ) ;  
                         }   c a t c h   ( P r o p e r t y V e t o E x c e p t i o n   p v e )   {  
                                 p v e . p r i n t S t a c k T r a c e ( ) ;  
                         }  
                 }  
         }  
  
         p u b l i c   v o i d   r e s e t I D C o n c e p t M a p ( )   {  
                 i d C o n c e p t M a p . c l e a r ( ) ;  
         }  
  
         p u b l i c   S e t   g e t A l l C o n c e p t ( )   {  
                 r e t u r n   i d C o n c e p t M a p . k e y S e t ( ) ;  
         }  
  
         p u b l i c   v o i d   p u t C o n c e p t ( S t r i n g   i d ,   C o n c e p t   c )   {  
                 i d C o n c e p t M a p . p u t ( c . g e t I d ( ) ,   c ) ;  
         }  
  
         p u b l i c   C o n c e p t   g e t C o n c e p t ( S t r i n g   i d )   {  
                 r e t u r n   i d C o n c e p t M a p . g e t ( i d ) ;  
         }  
  
         p u b l i c   v o i d   i n i t U s e r I D C o u n t ( )   {  
                 u s e r I D C o u n t   =   0 ;  
         }  
  
         p u b l i c   i n t   g e t U s e r I D C o u n t ( )   {  
                 r e t u r n   u s e r I D C o u n t ;  
         }  
  
         p u b l i c   S t r i n g   g e t U s e r I D S t r ( )   {  
                 r e t u r n   " U I D "   +   I n t e g e r . t o S t r i n g ( u s e r I D C o u n t + + ) ;  
         }  
  
         p u b l i c   v o i d   s e t U s e r I D C o u n t ( i n t   i d )   {  
                 i f   ( u s e r I D C o u n t   <   i d )   {  
                         u s e r I D C o u n t   =   i d ;  
                 }  
         }  
  
         p u b l i c   O n t o l o g y S e l e c t i o n P a n e l   g e t O n t o l o g y S e l e c t i o n P a n e l ( )   {  
                 r e t u r n   o n t S e l e c t i o n P a n e l ;  
         }  
  
         p u b l i c   D o c u m e n t S e l e c t i o n P a n e l   g e t D o c u m e n t S e l e c t i o n P a n e l ( )   {  
                 r e t u r n   d o c S e l e c t i o n P a n e l ;  
         }  
  
         p u b l i c   I n p u t W o r d S e l e c t i o n P a n e l   g e t I n p u t W o r d S e l e c t i o n P a n e l ( )   {  
                 r e t u r n   i n p u t W o r d S e l e c t i n P a n e l ;  
         }  
  
         p u b l i c   I n p u t M o d u l e U I   g e t I n p u t M o d u l e U I ( )   {  
                 r e t u r n   i n p u t M o d u l e U I ;  
         }  
  
         p u b l i c   I n p u t M o d u l e   g e t I n p u t M o d u l e ( )   {  
                 r e t u r n   i n p u t M o d u l e U I . g e t I n p u t M o d u l e ( ) ;  
         }  
  
         p u b l i c   C o n s t r u c t P r o p e r t y T r e e P a n e l   g e t C o n s t r u c t P r o p e r t y T r e e P a n e l ( )   {  
                 r e t u r n   c o n s t r u c t P r o p e r t y T r e e P a n e l ;  
         }  
  
         p u b l i c   C o n s t r u c t C o n c e p t T r e e P a n e l   g e t C o n s t r u c t C o n c e p t T r e e P a n e l ( )   {  
                 r e t u r n   c o n s t r u c t C o n c e p t T r e e P a n e l ;  
         }  
  
         p u b l i c   T e x t C o n c e p t D e f i n i t i o n P a n e l   g e t T e x t C o n c e p t D e f i n i t i o n P a n e l ( )   {  
                 r e t u r n   t e x t C o n c e p t D e f i n i t i o n P a n e l ;  
         }  
  
         p u b l i c   v o i d   s e t S e l e c t e d I n d e x ( i n t   i )   {  
                 t a b b e d P a n e . s e t S e l e c t e d I n d e x ( i ) ;  
         }  
  
         p u b l i c   b o o l e a n   i s P e r f e c t M a t c h e d A m b i g u i t y C n t C h e c k B o x ( )   {  
                 r e t u r n   i n p u t M o d u l e U I . i s P e r f e c t M a t c h e d A m b i g u i t y C n t C h e c k B o x ( ) ;  
         }  
  
         p u b l i c   b o o l e a n   i s P a r t i a l M a t c h e d A m b i g u i t y C n t C h e c k B o x ( )   {  
                 r e t u r n   i n p u t M o d u l e U I . i s P a r t i a l M a t c h e d A m b i g u i t y C n t C h e c k B o x ( ) ;  
         }  
  
         p u b l i c   b o o l e a n   i s P a r t i a l M a t c h e d C o m p l e x W o r d C h e c k B o x ( )   {  
                 r e t u r n   i n p u t M o d u l e U I . i s P a r t i a l M a t c h e d C o m p l e x W o r d C h e c k B o x ( ) ;  
         }  
  
         p u b l i c   b o o l e a n   i s P a r t i a l M a t c h e d M a t c h e d W o r d B o x ( )   {  
                 r e t u r n   i n p u t M o d u l e U I . i s P a r t i a l M a t c h e d M a t c h e d W o r d B o x ( ) ;  
         }  
 }  
